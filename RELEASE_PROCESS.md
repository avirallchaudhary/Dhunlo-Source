# DHUNLO Permanent Release Governance & Verification Process

This document outlines the mandatory, immutable release governance and verification procedures for the **DHUNLO** multiplatform music streaming application. All contributors and automated agents must adhere to these policies for every release.

---

## 1. Dual-Repository Architecture & Ownership

DHUNLO is maintained across two official repositories owned strictly by **`avirallchaudhary`**:

1. **Source Repository:** [`avirallchaudhary/Dhunlo-Source`](https://github.com/avirallchaudhary/Dhunlo-Source)
   * Contains the complete multiplatform source code, build scripts, Gradle configurations, tests, and historical release tags (`v1.0.0`, `v1.0.1`, `v1.0.2`, etc.).
   * Governed under the **GNU General Public License v3.0 (GPL-3.0)**.
2. **Distribution Repository:** [`avirallchaudhary/Dhunlo`](https://github.com/avirallchaudhary/Dhunlo)
   * Hosts distributed production release APKs (`DHUNLO-v<version>-release.apk` and convenience alias `Dhunlo.apk`), user documentation, installation guides, and legal notices.
   * Mirrors GitHub Releases for direct downloads.

### Strict Ownership & Isolation Policy
* **Authorized Owner:** `avirallchaudhary`
* **Prohibited Remotes:** No local workspace, branch, or script may ever reference, track, or push to `Jus1Munda/Dhunlo` or any unauthorized forks.
* **Push Rule:** No automated tool or agent may push to remote repositories without explicit user authorization.

---

## 2. Git Commit Identity Standards

To ensure all Git commits map uniquely and correctly to the intended GitHub profile:

* **Display Name:** `Aviral Chaudhary`
* **Commit Email:** `352257339+avirallchaudhary@users.noreply.github.com`
  *(Official GitHub privacy-preserving no-reply address tied to GitHub User ID `352257339`)*

### Local Repository Configuration
Run in each repository clone:
```bash
git config --local user.name "Aviral Chaudhary"
git config --local user.email "352257339+avirallchaudhary@users.noreply.github.com"
```
Never modify global Git configurations without explicit user instruction.

---

## 3. Application Identity & Signing Credentials

Every release build must preserve the following identity invariants:

* **Package Identifier (Application ID):** `com.metrolist.music`
* **App Name:** `DHUNLO`
* **Signing Scheme:** Android APK Signature Scheme v2 / v3
* **Official Release Signing Certificate SHA-256 Digest:**
  ```text
  33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa
  ```
  *(Certificate rotating or replacing with mismatched keys breaks in-place app updates on existing user installations).*

---

## 4. Release Verification Script (`verify_release.py`)

A permanent, reusable verification script is located at:
`scripts/verify_release.py`

### Script Capabilities & Safety Guarantee
* Validates repository remotes, Git identities, Gradle version codes, APK manifest badging, APK signatures, root APK checksums, README links, and source tags.
* **Purely Read-Only:** The script performs zero network writes, pushes, commits, or file mutations.
* **Fail-Safe:** Stops immediately with exit code `1` if any check fails, halting the release process.

### Running the Verification Script
```bash
# Standard release check (e.g. for v1.0.2)
python scripts/verify_release.py --version 1.0.2

# Custom workspace / distribution paths:
python scripts/verify_release.py \
  --workspace "C:/path/to/Dhunlo-Workspace" \
  --dist "C:/path/to/Dhunlo-Distribution" \
  --source "C:/path/to/Dhunlo-Source-Workspace" \
  --version 1.0.2
```

---

## 5. End-to-End Release Checklist

Before any release can be finalized, the following steps must be completed in order:

### Phase A: Implementation & Testing
1. Implement and test all required bug fixes and features.
2. Run test suites:
   * `:shared:desktopTest`
   * `:androidApp:testDebugUnitTest`
   * `:desktopApp:jvmTest`
3. Assemble release APK:
   ```bash
   ./gradlew :androidApp:assembleRelease
   ```
4. Confirm APK badging (`versionName`, `versionCode`, `com.metrolist.music`) and certificate SHA-256 match official standards.

### Phase B: Release Notes & UI Consistency
1. Curate user-facing release notes in `UserFacingReleaseNotes.kt` (`V<version>_NOTES`).
2. Verify that pre-download (`UpdatesScreen.kt`) and post-update modal (`ReleaseNotesSheet.kt`) use unified `ReleaseNotesList` / `ReleaseNoteItemRow` components.
3. Confirm that technical details (SHA-256, cert hashes, APK sizes, commit hashes) are completely hidden from normal user view.

### Phase C: Distribution Repository Staging
1. Copy verified release APK to the distribution repository root as `DHUNLO-v<version>-release.apk`.
2. Update `Dhunlo.apk` convenience alias with the identical file.
3. Remove previous outdated root release APK.
4. Update `README.md` download links, file sizes, SHA-256 checksum, and corresponding source repository release tag link.

### Phase D: Automated Governance Audit
1. Execute `python scripts/verify_release.py --version <version>`.
2. Ensure all checks return `[PASS]` and script exits with status `0`.

### Phase E: Sign-off & Publish (Requires Explicit User Approval)
1. Present full audit results to the user.
2. Only after explicit user authorization:
   * Commit and push source changes to `avirallchaudhary/Dhunlo-Source` and create the release tag.
   * Commit and push root APK updates to `avirallchaudhary/Dhunlo`.
   * Create GitHub Release on `avirallchaudhary/Dhunlo` with attached APK assets.
