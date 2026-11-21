# DHUNLO v1.0.0 — Source-to-Binary Provenance Record

This document establishes the official provenance record linking the distributed **DHUNLO v1.0.0** Android Release APK to this exact corresponding source code snapshot, in compliance with Section 6 of the GNU General Public License v3.0.

---

## 1. Distributed Artifact Identity

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

---

## 2. Source Snapshot Provenance

* **Source Repository:** `https://github.com/avirallchaudhary/Dhunlo-Source`
* **Release Git Tag:** `v1.0.0`
* **Base Upstream Commit:** `1404ba751491965ae943ff1845d8d7e08a6f2093` (Metrolist v14.0.1 tag `v14.0.1`)
* **Development History Base:** Commit `b7122314dd6c1818206e5813f848e490adeea9a7` (`feat(storage): implement independent Dhunlo storage identity and namespace decoupling`)
* **Compilation Timestamp:** 21 November 2026, 10:45:12 UTC+05:30
* **Snapshot State:** Exact working tree state containing versionCode 156, DHUNLO branding, Liquid Glass media controls, and theme accessibility updates, identical to the files compiled during the official build.

---

## 3. Build Tooling & Environment Baseline

* **Operating System:** Windows 10/11 x64 (Build 26300)
* **Java Runtime:** OpenJDK / JBR 17+ (Java 21 compatible)
* **Gradle Version:** Gradle 9.8.2 (via Gradle Wrapper)
* **Android Gradle Plugin (AGP):** 9.5.1
* **Kotlin Multiplatform:** 2.4.21
* **Compose Multiplatform:** 1.13.0-rc01
* **Android Build Tools:** 36.0.0
* **Android NDK:** 30.0.16248370
* **Opus Submodule Commit:** `a8b2545eec71e0f53ecead2d583e7b6baa42684d`
* **Metroproto Submodule Commit:** `c65557e90e90c23e62101e69bbdc98af9f6c7ce5`

---

## 4. Verification Instructions

To verify that this source snapshot corresponds to the published APK:
1. Follow the compilation steps in [BUILDING.md](BUILDING.md) to produce `androidApp-release.apk`.
2. Inspect the assembled APK metadata:
   ```bash
   aapt2 dump badging androidApp/build/outputs/apk/release/androidApp-release.apk
   ```
3. Verify that `package: name='com.metrolist.music' versionCode='156' versionName='1.0.0'` and `application-label:'Dhunlo'` match the distributed package.
4. Verify signature:
   ```bash
   apksigner verify --verbose --print-certs androidApp/build/outputs/apk/release/androidApp-release.apk
   ```
