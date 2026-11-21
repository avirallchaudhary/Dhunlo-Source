# DHUNLO — Source-to-Binary Provenance Record

This document establishes the official provenance record linking distributed **DHUNLO** Android Release APKs to their exact corresponding source code snapshots, in compliance with Section 6 of the GNU General Public License v3.0.

---

## 1. DHUNLO v1.0.1 Provenance Record

### Distributed Artifact Identity
* **Artifact File Names:** `DHUNLO-v1.0.1-release.apk` (and identical fallback alias `Dhunlo.apk`)
* **Distribution Repository:** `https://github.com/avirallchaudhary/Dhunlo`
* **Package Identifier:** `com.metrolist.music`
* **Version Name:** `1.0.1`
* **Version Code:** `157`
* **Artifact File Size:** `15,908,160` bytes (~15.17 MB)
* **SHA-256 Digest:**
  ```text
  C38620E28098BB0523CD7105255C88E0AB21BD8A8A3855A28827D617869EB30F
  ```
* **Signing Scheme:** APK Signature Scheme v2 (`true`)
* **Signing Certificate SHA-256 Digest:**
  ```text
  33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa
  ```

### Source Snapshot Provenance
* **Source Repository:** `https://github.com/avirallchaudhary/Dhunlo-Source`
* **Release Git Tag:** `v1.0.1`
* **Target Git Branch:** `main`
* **Base Upstream Commit:** `1404ba751491965ae943ff1845d8d7e08a6f2093` (Metrolist v14.0.1 tag `v14.0.1`)
* **Prior Source Commit:** `d5468e5` (Tag `v1.0.0`)
* **Compilation Timestamp:** 21 November 2026, 13:08:55 UTC+05:30
* **Snapshot State:** Exact corresponding source containing versionCode 157, versionName 1.0.1, hardened auto-updater resolution and validation logic, unit and regression test suites, identical to the source compiled for the official build.

### Build Tooling Baseline
* **Operating System:** Windows 10/11 x64 (Build 26300)
* **Java Runtime:** OpenJDK / JBR 17+ (Java 21 compatible)
* **Gradle Version:** Gradle 9.8.2 (via Gradle Wrapper)
* **Android Gradle Plugin (AGP):** 9.5.1
* **Kotlin Multiplatform:** 2.4.21
* **Compose Multiplatform:** 1.13.0-rc01
* **Android Build Tools:** 35.0.0 / 36.0.0
* **Android NDK:** 30.0.16248370
* **Opus Submodule Commit:** `a8b2545eec71e0f53ecead2d583e7b6baa42684d`
* **Metroproto Submodule Commit:** `c65557e90e90c23e62101e69bbdc98af9f6c7ce5`

---

## 2. Historical Record: DHUNLO v1.0.0

### Distributed Artifact Identity
* **Artifact File Name:** `DHUNLO-v1.0.0-release.apk`
* **Distribution Repository:** `https://github.com/avirallchaudhary/Dhunlo`
* **Package Identifier:** `com.metrolist.music`
* **Version Name:** `1.0.0`
* **Version Code:** `156`
* **Artifact File Size:** `15,905,542` bytes (~15.17 MB)
* **SHA-256 Digest:**
  ```text
  234BC8A078D9D173586F34CB8ADD08F9909D5557BF2C5448BA7CC710EB650836
  ```
* **Signing Scheme:** APK Signature Scheme v2 (`true`)
* **Signing Certificate SHA-256 Digest:**
  ```text
  33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa
  ```

### Source Snapshot Provenance
* **Source Repository:** `https://github.com/avirallchaudhary/Dhunlo-Source`
* **Release Git Tag:** `v1.0.0`
* **Base Upstream Commit:** `1404ba751491965ae943ff1845d8d7e08a6f2093` (Metrolist v14.0.1 tag `v14.0.1`)
* **Development History Base:** Commit `b7122314dd6c1818206e5813f848e490adeea9a7`
* **Compilation Timestamp:** 21 November 2026, 10:45:12 UTC+05:30

---

## 3. Verification Instructions

To verify that this source snapshot corresponds to the published v1.0.1 APK:
1. Follow the compilation steps in [BUILDING.md](BUILDING.md) to produce `androidApp-release.apk`.
2. Inspect the assembled APK metadata:
   ```bash
   aapt2 dump badging androidApp/build/outputs/apk/release/androidApp-release.apk
   ```
3. Verify that `package: name='com.metrolist.music' versionCode='157' versionName='1.0.1'` and `application-label:'Dhunlo'` match the distributed package.
4. Verify signature:
   ```bash
   apksigner verify --verbose --print-certs androidApp/build/outputs/apk/release/androidApp-release.apk
   ```
