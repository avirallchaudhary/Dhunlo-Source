# Legal Notices and Upstream Attribution

## 1. Upstream Project & Copyright Notice

**DHUNLO** is a modified fork of the open-source **Metrolist** project (specifically based on Metrolist v14.0.1, commit `1404ba751491965ae943ff1845d8d7e08a6f2093`).

* **Original Upstream Project:** [Metrolist](https://github.com/MetrolistGroup/Metrolist)
* **Upstream Copyright:** Copyright &copy; 2026 Metrolist contributors (including creator Mo Agamy, project leads Lamp (`l6t9`), Nyx (`nyxiereal`), and community contributors).
* **Upstream License:** GNU General Public License v3.0 (GPL-3.0). See [LICENSE](LICENSE).

## 2. DHUNLO Modifications & Copyright

In compliance with Section 5(a) of the GNU General Public License v3.0, the modifications introduced in DHUNLO include:

* **Modifications Copyright:** Copyright &copy; 2026 DHUNLO contributors (Avik Chaudhary / `avirallchaudhary`).
* **Relevant Dates:** November 2026.
* **Scope of Modifications:**
  * Independent DHUNLO brand identity, application labels, and custom adaptive vector/bitmap icons.
  * System UI & notification lock-screen liquid glass enhancements with real-time blur and dynamic scrims.
  * System volume panel branding integration.
  * Light and dark theme accessible contrast tuning across collection tiles and headers.
  * Independent storage identity and decoupling of application preferences.
  * Independent release update checking decoupled from upstream binaries.
* **License:** The entire DHUNLO work, including all modifications, is licensed as a whole under the GNU General Public License v3.0.

## 3. Third-Party Licenses & Bundled Components

DHUNLO includes and links to third-party open-source components under permissive licenses compatible with GPL-3.0:

### A. Opus Native Decoder (`opusDecoder/src/main/jni/libopus`)
* **Project:** Xiph.Org Foundation Opus interactive audio codec
* **Copyright:** Copyright &copy; 2001–2023 Xiph.Org, Skype Limited, Octasic, Jean-Marc Valin, Timothy B. Terriberry, CSIRO, Gregory Maxwell, Mark Borgerding, Erik de Castro Lopo, Mozilla, Amazon.
* **License:** BSD 3-Clause License (with royalty-free patent grants). See `opusDecoder/src/main/resources/META-INF/LICENSE-libopus.txt`.

### B. Google Protocol Buffers (`metroproto`)
* **Project:** Protocol Buffers
* **Copyright:** Copyright &copy; 2008 Google Inc.
* **License:** BSD 3-Clause License.

### C. Glyphmind Romanizer & Linguistic Datasets
Bundled in `shared/src/commonMain/composeResources/files/licenses/glyphmind/`:
* **Kuromoji & IPADIC:** Copyright &copy; 2010–2015 Atilika Inc. (Apache-2.0) and Copyright &copy; 2000–2003 Nara Institute of Science and Technology (NAIST / ICOT terms).
* **Mandarin & Unicode Unihan:** Copyright &copy; Unicode, Inc. (Unicode License V3) and houbb/pinyin (Apache-2.0).
* **FLORES-200 Sentence Evaluation Samples:** Meta AI (Creative Commons Attribution-ShareAlike 4.0 International, CC-BY-SA-4.0).
* **Tatoeba Linguistic Corpus:** Tatoeba contributors (Creative Commons Attribution 2.0 France, CC-BY-2.0-FR).

### D. Permissive Runtime & Multiplatform Libraries
* **AndroidX Framework & Media3 / ExoPlayer:** Copyright &copy; The Android Open Source Project. Licensed under the Apache License, Version 2.0.
* **Kotlin & Compose Multiplatform:** Copyright &copy; 2010–2026 JetBrains s.r.o. Licensed under the Apache License, Version 2.0.
* **Square OkHttp:** Copyright &copy; Square, Inc. Licensed under the Apache License, Version 2.0.
* **Ktor:** Copyright &copy; 2026 JetBrains s.r.o. Licensed under the Apache License, Version 2.0.
* **Coil 3:** Copyright &copy; 2026 Coil Contributors. Licensed under the Apache License, Version 2.0.
* **Koin:** Copyright &copy; 2026 Insert-Koin Contributors. Licensed under the Apache License, Version 2.0.
* **MaterialKolor:** Copyright &copy; Jordond. Licensed under the MIT License.
* **Sentry SDK:** Copyright &copy; 2026 Functional Software, Inc. dba Sentry. Licensed under the MIT License.
* **Timber, Shimmer, Swipe, Reorderable, Multiplatform Settings, Markdown Renderer:** Licensed under the Apache License, Version 2.0.
