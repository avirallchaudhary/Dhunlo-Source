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
    fun technical_metadata_is_filtered_out() {
        val technicalBody =
            """
            # DHUNLO v1.0.2 (build 158)
            - Package: com.metrolist.music
            - Version: 1.0.2
            - Version Code: 158
            - Upstream: Metrolist v14.0.1
            
            ## Release Assets
            - DHUNLO-v1.0.2-release.apk
              - Size: 15,908,160 bytes
              - SHA-256: C38620E28098BB0523CD7105255C88E0AB21BD8A8A3855A28827D617869EB30F
            
            ## Signing Verification
            - Certificate SHA-256: 33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa
            
            ## Source Code & Licensing
            - Corresponding source: https://github.com/avirallchaudhary/Dhunlo-Source/tree/v1.0.2
            - Licensed under GNU General Public License v3.0 (GPL-3.0)
            """.trimIndent()

        val notes = UserFacingReleaseNotes.getNotes("1.0.2", technicalBody)
        // Since all lines were technical metadata, fallback is returned
        assertEquals(1, notes.size)
        assertEquals("Performance & Stability", notes[0].title)
        assertTrue(notes[0].description.isNotEmpty())
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
