# DHUNLO — Source Code Repository

This repository contains the complete, corresponding **Source Code** for the **DHUNLO** application, published in full compliance with Section 6 of the **GNU General Public License v3.0** (GPL-3.0).

---

## Release Identification & Binary Provenance

* **Application Name:** DHUNLO
* **Current Release Version:** `1.0.2`
* **Android Version Code:** `158`
* **Package Identifier:** `com.metrolist.music`
* **Target Git Tag:** `v1.0.2`
* **Distributed Release APK:** [`DHUNLO-v1.0.2-release.apk`](https://github.com/avirallchaudhary/Dhunlo) (and fallback alias `Dhunlo.apk`)
* **Official APK SHA-256 Checksum:**
  ```text
  4935111D26CD8F682FD461DB85998B1BA6F8608F9B95CF02B823F4FAF9CDE977
  ```
* **Official Signing Certificate SHA-256:**
  ```text
  33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa
  ```

---

## Release History

* **DHUNLO v1.0.2** (Version Code `158`, Git Tag `v1.0.2`):
  * Official Release APK: `DHUNLO-v1.0.2-release.apk`
  * SHA-256: `4935111D26CD8F682FD461DB85998B1BA6F8608F9B95CF02B823F4FAF9CDE977`
  * Changes: Critical playback stability (delayed audio focus recovery, transient MediaSource network retry loop, fast audio-to-video mode switching, next-track preloading, and video surface screen keep-on flag), Compose recomposition and artwork performance optimizations, and official v1.0.2 release identity.
* **DHUNLO v1.0.1** (Version Code `157`, Git Tag `v1.0.1`):
  * Official Release APK: `DHUNLO-v1.0.1-release.apk`
  * SHA-256: `C38620E28098BB0523CD7105255C88E0AB21BD8A8A3855A28827D617869EB30F`
  * Changes: Hardened in-app auto-updater release resolution, certificate validation, redirect sanitization, and release channel verification.
* **DHUNLO v1.0.0** (Version Code `156`, Git Tag `v1.0.0`):
  * Official Release APK: `DHUNLO-v1.0.0-release.apk`
  * SHA-256: `234BC8A078D9D173586F34CB8ADD08F9909D5557BF2C5448BA7CC710EB650836`
  * Initial public release with DHUNLO branding, Liquid Glass UI, decoupled storage identity, and standalone release distribution.

---

## Upstream Provenance & Attribution

DHUNLO is an independent, enhanced fork derived from the open-source **Metrolist** project (baseline Metrolist v14.0.1).

* **Original Upstream Repository:** [https://github.com/MetrolistGroup/Metrolist](https://github.com/MetrolistGroup/Metrolist)
* **Upstream Copyright:** Copyright &copy; 2026 Metrolist contributors (Creator: Mo Agamy; Leads: Lamp, Nyx; and community authors).
* **Modifications Copyright:** Copyright &copy; 2026 DHUNLO contributors (Avik Chaudhary / `avirallchaudhary`).
* For detailed legal notices, third-party licenses, and modification records, refer to [NOTICE.md](NOTICE.md).

---

## Building from Source

Complete build prerequisites, dependencies, NDK setup, and compilation commands for assembling the Android application are documented in **[BUILDING.md](BUILDING.md)**.

Quick build command:
```bash
./gradlew :androidApp:assembleRelease
```

---

## Provenance & Verification

To review the exact source snapshot provenance, commit mapping, and APK hash audit records, see **[PROVENANCE.md](PROVENANCE.md)**.

---

## License

DHUNLO and its corresponding source code are licensed under the **GNU General Public License v3.0** (GPL-3.0).  
See the full license text in the [LICENSE](LICENSE) file.
