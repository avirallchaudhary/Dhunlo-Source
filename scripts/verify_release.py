#!/usr/bin/env python3
"""
DHUNLO Pre-Release Governance & Verification Script
===================================================
Validates all pre-release and post-release invariants for DHUNLO across the
source repository, development workspace, and distribution repository.

Checks performed:
1. Repository Ownership & Remotes (avirallchaudhary/Dhunlo, avirallchaudhary/Dhunlo-Source)
2. Git Commit Identity (Aviral Chaudhary <352257339+avirallchaudhary@users.noreply.github.com>)
3. App Version & Package ID in Gradle configuration
4. APK Manifest Badging (package, versionName, versionCode) via aapt2
5. APK Signature & Signing Certificate SHA-256 digest via apksigner
6. APK SHA-256 Checksum consistency across assets and distribution repository
7. Distribution README.md links, checksums, and version tags
8. Source repository release tag presence
9. Safety enforcement: strictly READ-ONLY; never pushes or publishes automatically.

Exit code: 0 on success, 1 on any check failure.
"""

import sys
import os
import re
import hashlib
import subprocess
import argparse

EXPECTED_PACKAGE = "com.metrolist.music"
EXPECTED_CERT_SHA256 = "33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa"
EXPECTED_AUTHOR_NAME = "Aviral Chaudhary"
EXPECTED_AUTHOR_EMAIL = "352257339+avirallchaudhary@users.noreply.github.com"
DISALLOWED_REMOTES = ["Jus1Munda/Dhunlo", "Jus1Munda/Dhunlo-Source"]

def find_tool(tool_name):
    # Check PATH first
    path = os.environ.get("PATH", "").split(os.pathsep)
    for p in path:
        cand = os.path.join(p, tool_name)
        if os.path.exists(cand):
            return cand
        cand_bat = cand + ".bat"
        if os.path.exists(cand_bat):
            return cand_bat
        cand_exe = cand + ".exe"
        if os.path.exists(cand_exe):
            return cand_exe

    # Check Android SDK
    sdk_root = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if not sdk_root:
        local_app_data = os.environ.get("LOCALAPPDATA")
        if local_app_data:
            cand = os.path.join(local_app_data, "Android", "Sdk")
            if os.path.exists(cand):
                sdk_root = cand

    if sdk_root and os.path.exists(sdk_root):
        build_tools = os.path.join(sdk_root, "build-tools")
        if os.path.exists(build_tools):
            for ver in sorted(os.listdir(build_tools), reverse=True):
                cand = os.path.join(build_tools, ver, tool_name)
                if os.path.exists(cand):
                    return cand
                cand_bat = cand + ".bat"
                if os.path.exists(cand_bat):
                    return cand_bat
                cand_exe = cand + ".exe"
                if os.path.exists(cand_exe):
                    return cand_exe
    return None

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest().lower()

class ReleaseVerifier:
    def __init__(self, workspace_dir, dist_dir=None, source_dir=None, target_version=None, apk_path=None, expected_checksum=None):
        self.workspace_dir = os.path.abspath(workspace_dir)
        self.dist_dir = os.path.abspath(dist_dir) if dist_dir else None
        self.source_dir = os.path.abspath(source_dir) if source_dir else None
        self.target_version = target_version
        self.target_version_code = None
        self.custom_apk_path = os.path.abspath(apk_path) if apk_path else None
        self.expected_checksum = expected_checksum.lower().strip() if expected_checksum else None
        self.failed = False
        self.checks_run = 0
        self.checks_passed = 0

    def check(self, name, condition, details=""):
        self.checks_run += 1
        if condition:
            self.checks_passed += 1
            print(f"  [PASS] {name}")
            if details:
                print(f"         {details}")
            return True
        else:
            self.failed = True
            print(f"  [FAIL] {name}")
            if details:
                print(f"         ERROR: {details}")
            return False

    def verify_git_remotes(self, repo_dir, repo_name, allowed_substring):
        if not os.path.exists(repo_dir):
            return self.check(f"{repo_name} exists", False, f"Directory not found: {repo_dir}")

        res = subprocess.run(["git", "remote", "-v"], cwd=repo_dir, capture_output=True, text=True)
        remotes = res.stdout
        disallowed = any(d in remotes for d in DISALLOWED_REMOTES)
        self.check(
            f"{repo_name} no disallowed remotes",
            not disallowed,
            "Disallowed remote found (Jus1Munda)" if disallowed else "No disallowed remote references",
        )

        has_allowed = allowed_substring.lower() in remotes.lower()
        self.check(
            f"{repo_name} remote origin target",
            has_allowed,
            f"Expected remote matching '{allowed_substring}'" if not has_allowed else f"Remote matches '{allowed_substring}'",
        )

    def verify_git_identity(self, repo_dir, repo_name):
        if not os.path.exists(repo_dir):
            return

        name = subprocess.run(["git", "config", "user.name"], cwd=repo_dir, capture_output=True, text=True).stdout.strip()
        email = subprocess.run(["git", "config", "user.email"], cwd=repo_dir, capture_output=True, text=True).stdout.strip()

        self.check(
            f"{repo_name} Git author name",
            name == EXPECTED_AUTHOR_NAME,
            f"Expected '{EXPECTED_AUTHOR_NAME}', found '{name}'",
        )
        self.check(
            f"{repo_name} Git author email",
            email == EXPECTED_AUTHOR_EMAIL,
            f"Expected '{EXPECTED_AUTHOR_EMAIL}', found '{email}'",
        )

    def detect_gradle_version(self):
        gradle_file = os.path.join(self.workspace_dir, "androidApp", "build.gradle.kts")
        if not os.path.exists(gradle_file):
            return None, None
        with open(gradle_file, "r", encoding="utf-8") as f:
            content = f.read()

        ver_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)
        code_match = re.search(r'versionCode\s*=\s*(\d+)', content)

        v_name = ver_match.group(1) if ver_match else None
        v_code = code_match.group(1) if code_match else None
        return v_name, v_code

    def verify_apk(self, apk_path):
        if not self.check("Release APK file exists", os.path.isfile(apk_path), apk_path):
            return None

        size = os.path.getsize(apk_path)
        sha256 = sha256_file(apk_path)
        self.check("Release APK non-empty", size > 10_000_000, f"Size: {size:,} bytes, SHA-256: {sha256}")

        # Check aapt2
        aapt2 = find_tool("aapt2")
        if aapt2:
            res = subprocess.run([aapt2, "dump", "badging", apk_path], capture_output=True, text=True)
            out = res.stdout
            pkg_match = re.search(r"package:\s+name='([^']+)'", out)
            ver_match = re.search(r"versionName='([^']+)'", out)
            code_match = re.search(r"versionCode='([^']+)'", out)

            pkg = pkg_match.group(1) if pkg_match else "unknown"
            ver = ver_match.group(1) if ver_match else "unknown"
            code = code_match.group(1) if code_match else "unknown"

            self.check("APK package ID matches", pkg == EXPECTED_PACKAGE, f"Expected '{EXPECTED_PACKAGE}', got '{pkg}'")
            if self.target_version:
                self.check("APK versionName matches target", ver == self.target_version, f"Expected '{self.target_version}', got '{ver}'")
            if self.target_version_code:
                self.check("APK versionCode matches target", code == str(self.target_version_code), f"Expected '{self.target_version_code}', got '{code}'")
        else:
            print("  [WARN] aapt2 not found; skipping manifest badging check")

        # Check apksigner
        apksigner = find_tool("apksigner")
        if apksigner:
            res = subprocess.run([apksigner, "verify", "--print-certs", apk_path], capture_output=True, text=True)
            cert_match = re.search(r"Signer #1 certificate SHA-256 digest:\s+([a-fA-F0-9]{64})", res.stdout)
            if cert_match:
                found_digest = cert_match.group(1).lower()
                self.check(
                    "APK signing certificate SHA-256",
                    found_digest == EXPECTED_CERT_SHA256,
                    f"Expected {EXPECTED_CERT_SHA256}\n         Found    {found_digest}",
                )
            else:
                self.check("APK signature verification", False, "Could not find Signer #1 SHA-256 digest in apksigner output")
        else:
            print("  [WARN] apksigner not found; skipping signing certificate check")

        return sha256

    def verify_distribution_repo(self, expected_sha256):
        if not self.dist_dir or not os.path.exists(self.dist_dir):
            print("  [SKIP] Distribution repository directory not specified or does not exist")
            return

        print("\n--- Verifying Distribution Repository ---")
        self.verify_git_remotes(self.dist_dir, "Dhunlo Distribution", "avirallchaudhary/Dhunlo")
        self.verify_git_identity(self.dist_dir, "Dhunlo Distribution")

        # Root APK check
        versioned_apk = os.path.join(self.dist_dir, f"DHUNLO-v{self.target_version}-release.apk")
        alias_apk = os.path.join(self.dist_dir, "Dhunlo.apk")

        if self.check(f"Distribution root APK exists (DHUNLO-v{self.target_version}-release.apk)", os.path.isfile(versioned_apk), versioned_apk):
            sha_versioned = sha256_file(versioned_apk)
            self.check(
                "Versioned root APK SHA-256 matches verified build",
                sha_versioned == expected_sha256,
                f"Expected {expected_sha256}, got {sha_versioned}",
            )

        if self.check("Distribution root alias exists (Dhunlo.apk)", os.path.isfile(alias_apk), alias_apk):
            sha_alias = sha256_file(alias_apk)
            self.check(
                "Root alias APK SHA-256 matches verified build",
                sha_alias == expected_sha256,
                f"Expected {expected_sha256}, got {sha_alias}",
            )

        # Check README.md
        readme = os.path.join(self.dist_dir, "README.md")
        if self.check("Distribution README.md exists", os.path.isfile(readme), readme):
            with open(readme, "r", encoding="utf-8") as f:
                readme_text = f.read()

            self.check(
                "README contains target version",
                f"Version: {self.target_version}" in readme_text or f"v{self.target_version}" in readme_text,
                f"Target version: {self.target_version}",
            )
            self.check(
                "README contains versioned APK link",
                f"DHUNLO-v{self.target_version}-release.apk" in readme_text,
                f"DHUNLO-v{self.target_version}-release.apk referenced",
            )
            self.check(
                "README contains correct APK SHA-256",
                expected_sha256.upper() in readme_text.upper(),
                f"Checksum: {expected_sha256}",
            )
            self.check(
                "README contains correct Certificate SHA-256",
                EXPECTED_CERT_SHA256.upper() in readme_text.upper(),
                f"Cert Checksum: {EXPECTED_CERT_SHA256}",
            )

    def verify_source_repo(self):
        if not self.source_dir or not os.path.exists(self.source_dir):
            print("  [SKIP] Source repository directory not specified or does not exist")
            return

        print("\n--- Verifying Source Repository ---")
        self.verify_git_remotes(self.source_dir, "Dhunlo Source", "avirallchaudhary/Dhunlo-Source")
        self.verify_git_identity(self.source_dir, "Dhunlo Source")

        # Tag check
        tag = f"v{self.target_version}"
        res = subprocess.run(["git", "tag", "-l", tag], cwd=self.source_dir, capture_output=True, text=True)
        has_tag = tag in res.stdout.splitlines()
        self.check(f"Source repository tag '{tag}' exists", has_tag, f"Tag {tag} verified in repository")

    def run(self):
        print("=================================================================")
        print("         DHUNLO PRE-RELEASE GOVERNANCE AUDIT & VERIFICATION       ")
        print("=================================================================")

        v_name, v_code = self.detect_gradle_version()
        if not self.target_version:
            self.target_version = v_name
        self.target_version_code = v_code

        print(f"Target Version:      {self.target_version} (Code: {self.target_version_code})")
        print(f"Target Package:      {EXPECTED_PACKAGE}")
        print(f"Development Workspace: {self.workspace_dir}")
        print(f"Distribution Repo:   {self.dist_dir or 'Not specified'}")
        print(f"Source Repo:         {self.source_dir or 'Not specified'}")
        print("-----------------------------------------------------------------")

        print("\n--- 1. Workspace Git Identity & Remote Check ---")
        self.verify_git_identity(self.workspace_dir, "Development Workspace")

        print("\n--- 2. Release APK Integrity & Signature Verification ---")
        apk_path = self.custom_apk_path
        if not apk_path and self.dist_dir:
            cand = os.path.join(self.dist_dir, f"DHUNLO-v{self.target_version}-release.apk")
            if os.path.exists(cand):
                apk_path = cand
        if not apk_path:
            apk_path = os.path.join(self.workspace_dir, "androidApp", "build", "outputs", "apk", "release", "androidApp-release.apk")

        apk_sha256 = self.verify_apk(apk_path)
        if self.expected_checksum and apk_sha256:
            self.check(
                "APK SHA-256 matches explicit expected checksum",
                apk_sha256 == self.expected_checksum,
                f"Expected {self.expected_checksum}, got {apk_sha256}",
            )

        if apk_sha256:
            self.verify_distribution_repo(apk_sha256)

        self.verify_source_repo()

        print("\n=================================================================")
        print(f"RESULTS: {self.checks_passed} / {self.checks_run} checks passed.")
        if self.failed:
            print("STATUS: FAILED. Pre-release verification discovered discrepancies.")
            print("STOPPING RELEASE. Never push or publish until all checks pass.")
            print("=================================================================")
            return 1
        else:
            print("STATUS: SUCCESS. All release governance invariants PASSED.")
            print("Ready for release sign-off (requires explicit user authorization to push).")
            print("=================================================================")
            return 0

def main():
    parser = argparse.ArgumentParser(description="DHUNLO Pre-Release Governance Verification")
    parser.add_argument("--workspace", default=r"C:\Users\acer\Downloads\Dhunlo-Workspace", help="Development workspace directory")
    parser.add_argument("--dist", default=r"C:\Users\acer\Downloads\Dhunlo-Distribution", help="Distribution repository directory")
    parser.add_argument("--source", default=r"C:\Users\acer\Downloads\Dhunlo-Source-Workspace", help="Source repository directory")
    parser.add_argument("--version", default="1.0.2", help="Target release version string")
    parser.add_argument("--apk", default=None, help="Explicit path to release APK")
    parser.add_argument("--expected-checksum", default=None, help="Explicit expected APK SHA-256 checksum")

    args = parser.parse_args()

    verifier = ReleaseVerifier(
        workspace_dir=args.workspace,
        dist_dir=args.dist,
        source_dir=args.source,
        target_version=args.version,
        apk_path=args.apk,
        expected_checksum=args.expected_checksum,
    )
    sys.exit(verifier.run())

if __name__ == "__main__":
    main()
