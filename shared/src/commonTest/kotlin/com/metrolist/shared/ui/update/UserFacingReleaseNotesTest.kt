package com.metrolist.shared.ui.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserFacingReleaseNotesTest {
    @Test
    fun v1_0_1_returns_curated_user_facing_notes() {
        val notes = UserFacingReleaseNotes.getNotes("1.0.1", null)
        assertEquals(4, notes.size)
        assertEquals("More Reliable Updates", notes[0].title)
        assertEquals("Improved update detection and download reliability.", notes[0].description)
        assertEquals("Enhanced Update Security", notes[1].title)
        assertEquals("Added stronger verification before installing updates.", notes[1].description)
        assertEquals("Safer Releases", notes[2].title)
        assertEquals("Improved protection against unintended experimental updates.", notes[2].description)
        assertEquals("Seamless Upgrades", notes[3].title)
        assertEquals("Improved in-place updates while preserving music data, preferences and offline queues.", notes[3].description)
    }

    @Test
    fun v1_0_1_with_v_prefix_returns_curated_user_facing_notes() {
        val notes = UserFacingReleaseNotes.getNotes("v1.0.1", "# Technical Body with SHA-256")
        assertEquals(4, notes.size)
        assertEquals("More Reliable Updates", notes[0].title)
    }

    @Test
    fun v1_0_2_returns_curated_user_facing_notes() {
        val notes = UserFacingReleaseNotes.getNotes("1.0.2", null)
        assertEquals(6, notes.size)
        assertEquals("Screen-Off Audio Playback", notes[0].title)
        assertEquals("Network Disconnection Recovery", notes[1].title)
        assertEquals("Faster Track Transitions", notes[2].title)
        assertEquals("Instant Video Mode", notes[3].title)
        assertEquals("Display Wake Lock for Videos", notes[4].title)
        assertEquals("Smooth Scrolling & Performance", notes[5].title)
    }

    @Test
    fun v1_0_2_with_v_prefix_returns_curated_user_facing_notes() {
        val notes = UserFacingReleaseNotes.getNotes("v1.0.2", "# Technical Body with SHA-256")
        assertEquals(6, notes.size)
        assertEquals("Screen-Off Audio Playback", notes[0].title)
    }

    @Test
    fun technical_metadata_is_filtered_out() {
        val technicalBody =
            """
            # DHUNLO v1.0.4 (build 159)
            - Package: com.metrolist.music
            - Version: 1.0.4
            - Version Code: 159
            - Upstream: Metrolist v14.0.1
            
            ## Release Assets
            - DHUNLO-v1.0.4-release.apk
              - Size: 15,908,160 bytes
              - SHA-256: C38620E28098BB0523CD7105255C88E0AB21BD8A8A3855A28827D617869EB30F
            
            ## Signing Verification
            - Certificate SHA-256: 33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa
            
            ## Source Code & Licensing
            - Corresponding source: https://github.com/avirallchaudhary/Dhunlo-Source/tree/v1.0.4
            - Licensed under GNU General Public License v3.0 (GPL-3.0)
            """.trimIndent()

        val notes = UserFacingReleaseNotes.getNotes("1.0.4", technicalBody)
        // Since all lines were technical metadata, fallback is returned
        assertEquals(1, notes.size)
        assertEquals("Performance & Stability", notes[0].title)
        assertTrue(notes[0].description.isNotEmpty())
    }

    @Test
    fun v1_0_2_release_body_parses_cleanly_via_parse_body() {
        val v102Body =
            """
            # DHUNLO v1.0.2

            Official release of **DHUNLO v1.0.2** for Android.

            ---

            ## Release Identification & Artifacts

            * **Application Name:** DHUNLO
            * **Version Name:** `1.0.2`
            * **Package Identifier:** `com.metrolist.music`

            ### Attached Assets
            1. **`DHUNLO-v1.0.2-release.apk`**
               * **Size:** `15,811,929 bytes`
               * **SHA-256 Checksum:**
                 ```text
                 4935111D26CD8F682FD461DB85998B1BA6F8608F9B95CF02B823F4FAF9CDE977
                 ```
            2. **`Dhunlo.apk`**
               * Attached as an identical fallback alias for backward-compatible in-app managers.

            ---

            ## What's New in v1.0.2

            **Playback and stability improvements**

            - Fixed unexpected playback interruptions during screen-off audio playback.
            - Improved recovery after temporary network disconnections via automatic retry budgets.
            - Improved next-track transition and preloading latency.
            - Improved audio-to-video mode transition and stream caching.
            - Fixed video-mode screen timeout behavior via `FLAG_KEEP_SCREEN_ON`.
            - Improved release UI scrolling responsiveness and artwork loading performance.

            ---

            ## Signing Certificate Verification
            * Certificate SHA-256: 33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa

            ---

            ## Corresponding Source Code & GPL-3.0 Compliance
            * Exact Commit SHA: 9413caf94b0126c909e14d2091e7c41b98d89c7d
            """.trimIndent()

        val parsed = UserFacingReleaseNotes.parseBody(v102Body)
        assertEquals(6, parsed.size)
        assertTrue(parsed.all { !it.title.contains("SHA-256") && !it.title.contains("com.metrolist") })
        assertEquals("Fixed unexpected playback interruptions during screen-off audio playback.", parsed[0].title)
    }

    @Test
    fun dynamic_release_notes_parsed_correctly() {
        val bodyWithChanges =
            """
            ## What's New
            - **Better Audio Equalizer** — Enhanced equalizer responsiveness and custom presets.
            - **Playlist Cover Collages** — Improved album artwork generation for local playlists.
            - Fixed audio playback glitch during Bluetooth disconnect.
            
            ## Technical Info
            - SHA-256: abcdef1234567890
            - Package: com.metrolist.music
            """.trimIndent()

        val notes = UserFacingReleaseNotes.getNotes("1.0.3", bodyWithChanges)
        assertEquals(3, notes.size)
        assertEquals("Better Audio Equalizer", notes[0].title)
        assertEquals("Enhanced equalizer responsiveness and custom presets.", notes[0].description)
        assertEquals("Playlist Cover Collages", notes[1].title)
        assertEquals("Improved album artwork generation for local playlists.", notes[1].description)
        assertEquals("Fixed audio playback glitch during Bluetooth disconnect.", notes[2].title)
    }

    @Test
    fun fallback_provided_when_body_is_empty() {
        val notes = UserFacingReleaseNotes.getNotes("2.0.0", "")
        assertEquals(1, notes.size)
        assertEquals("Performance & Stability", notes[0].title)
    }
}
