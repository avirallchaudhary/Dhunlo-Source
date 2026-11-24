package com.metrolist.shared.ui.update

import androidx.compose.ui.graphics.vector.ImageVector
import com.metrolist.shared.ui.icons.MetrolistIcons

data class UserFacingReleaseNote(
    val title: String,
    val description: String,
    val icon: ImageVector,
)

object UserFacingReleaseNotes {
    private val V1_0_1_NOTES =
        listOf(
            UserFacingReleaseNote(
                title = "More Reliable Updates",
                description = "Improved update detection and download reliability.",
                icon = MetrolistIcons.Outlined.Update,
            ),
            UserFacingReleaseNote(
                title = "Enhanced Update Security",
                description = "Added stronger verification before installing updates.",
                icon = MetrolistIcons.Outlined.Security,
            ),
            UserFacingReleaseNote(
                title = "Safer Releases",
                description = "Improved protection against unintended experimental updates.",
                icon = MetrolistIcons.Filled.Verified,
            ),
            UserFacingReleaseNote(
                title = "Seamless Upgrades",
                description = "Improved in-place updates while preserving music data, preferences and offline queues.",
                icon = MetrolistIcons.Filled.Sync,
            ),
        )

    private val V1_0_2_NOTES =
        listOf(
            UserFacingReleaseNote(
                title = "Screen-Off Audio Playback",
                description = "Fixed unexpected playback interruptions when the phone screen is turned off.",
                icon = MetrolistIcons.Outlined.BarChart,
            ),
            UserFacingReleaseNote(
                title = "Network Disconnection Recovery",
                description = "Automatic and seamless playback resumption when reconnecting to Wi-Fi or mobile networks.",
                icon = MetrolistIcons.Filled.Sync,
            ),
            UserFacingReleaseNote(
                title = "Faster Track Transitions",
                description = "Improved preloading and latency for immediate next-song playback.",
                icon = MetrolistIcons.Outlined.Update,
            ),
            UserFacingReleaseNote(
                title = "Instant Video Mode",
                description = "Optimized audio-to-video switching with streamlined stream caching.",
                icon = MetrolistIcons.Filled.NewReleases,
            ),
            UserFacingReleaseNote(
                title = "Display Wake Lock for Videos",
                description = "Prevents the display from sleeping while watching music videos.",
                icon = MetrolistIcons.Outlined.Security,
            ),
            UserFacingReleaseNote(
                title = "Smooth Scrolling & Performance",
                description = "Optimized scrolling responsiveness and faster artwork loading throughout the app.",
                icon = MetrolistIcons.Filled.Verified,
            ),
        )

    private val TECHNICAL_PATTERNS =
        listOf(
            "sha-256",
            "sha256",
            "sha-1",
            "sha1",
            "md5",
            "checksum",
            "digest",
            "certificate",
            "keystore",
            "public key",
            "fingerprint",
            "com.metrolist.music",
            "package:",
            "package id",
            "package name",
            "application name",
            "version:",
            "version code",
            "version name",
            "build code",
            "upstream:",
            "upstream provenance",
            "metrolist",
            ".apk",
            "apk:",
            "bytes",
            "size:",
            "targetsdk",
            "compilesdk",
            "minsdk",
            "release assets",
            "attached assets",
            "release identification",
            "signing verification",
            "signer dn",
            "certificate sha-256",
            "public key sha-256",
            "source code",
            "source tag",
            "source repository",
            "exact commit",
            "commit sha",
            "build instructions",
            "legal notices",
            "fallback alias",
            "compatibility alias",
            "provenance audit",
            "release package",
            "official release package",
            "github.com",
            "gpl",
            "general public license",
            "licensing",
            "license",
            "prerelease",
            "redirect sanitization",
            "redirect validation",
            "in-place upgrade verification",
            "build artifact",
            "provenance",
        )

    fun getNotes(
        version: String,
        rawBody: String?,
    ): List<UserFacingReleaseNote> {
        val cleanVersion = version.removePrefix("v").trim()
        if (cleanVersion == "1.0.1") {
            return V1_0_1_NOTES
        }
        if (cleanVersion == "1.0.2") {
            return V1_0_2_NOTES
        }

        val parsed = parseBody(rawBody)
        if (parsed.isNotEmpty()) {
            return parsed
        }

        return listOf(
            UserFacingReleaseNote(
                title = "Performance & Stability",
                description = "General improvements and fixes to make your Dhunlo experience smoother.",
                icon = MetrolistIcons.Filled.Verified,
            ),
        )
    }

    internal fun parseBody(rawBody: String?): List<UserFacingReleaseNote> {
        if (rawBody.isNullOrBlank()) return emptyList()

        val results = mutableListOf<UserFacingReleaseNote>()
        val lines = rawBody.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }

        var inTechnicalSection = false

        for (line in lines) {
            val lower = line.lowercase()

            if (line.startsWith("#")) {
                val headerText = lower.trimStart('#').trim()
                val isWhatsNewHeader = headerText.contains("what's new") ||
                    headerText.contains("whats new") ||
                    headerText.contains("changelog") ||
                    headerText.contains("highlight") ||
                    headerText.contains("changes") ||
                    headerText.contains("features") ||
                    headerText.contains("improvements") ||
                    headerText.contains("bug fix")
                if (isWhatsNewHeader) {
                    inTechnicalSection = false
                    continue
                }
                inTechnicalSection = TECHNICAL_PATTERNS.any { headerText.contains(it) } ||
                    headerText.contains("identification") ||
                    headerText.contains("artifact") ||
                    headerText.contains("asset") ||
                    headerText.contains("verification") ||
                    headerText.contains("signing") ||
                    headerText.contains("compliance") ||
                    headerText.contains("source code") ||
                    headerText.contains("details")
                continue
            }

            if (inTechnicalSection) continue
            if (TECHNICAL_PATTERNS.any { lower.contains(it) }) continue
            if (line.startsWith("|") || line.startsWith("---") || line.startsWith("***")) continue

            val bulletContent =
                when {
                    line.startsWith("- ") -> line.removePrefix("- ").trim()
                    line.startsWith("* ") -> line.removePrefix("* ").trim()
                    line.startsWith("• ") -> line.removePrefix("• ").trim()
                    line.length >= 3 && line[0].isDigit() && (line[1] == '.' || line[2] == '.') -> {
                        line.substringAfter('.').trim()
                    }
                    else -> null
                } ?: continue

            val note = parseNoteItem(bulletContent)
            if (note != null) {
                results.add(note)
            }
        }

        return results
    }

    private fun parseNoteItem(content: String): UserFacingReleaseNote? {
        val clean = content.trim()
        if (clean.isBlank()) return null
        val lower = clean.lowercase()
        if (TECHNICAL_PATTERNS.any { lower.contains(it) }) return null

        val (rawTitle, rawDesc) =
            when {
                clean.contains("—") -> {
                    clean.substringBefore("—").trim() to clean.substringAfter("—").trim()
                }
                clean.contains(" - ") -> {
                    clean.substringBefore(" - ").trim() to clean.substringAfter(" - ").trim()
                }
                clean.contains(":") && clean.indexOf(":") < 40 -> {
                    clean.substringBefore(":").trim() to clean.substringAfter(":").trim()
                }
                else -> {
                    clean to ""
                }
            }

        val title = rawTitle
            .removePrefix("**").removeSuffix("**")
            .removePrefix("__").removeSuffix("__")
            .removePrefix("`").removeSuffix("`")
            .trim()
        val desc = rawDesc
            .removePrefix("**").removeSuffix("**")
            .removePrefix("__").removeSuffix("__")
            .removePrefix("`").removeSuffix("`")
            .trim()

        if (title.isBlank()) return null
        val lowerTitle = title.lowercase()
        if (lowerTitle in listOf(
                "version", "package", "upstream", "build", "size", "sha-256", "sha256", "checksum",
                "application name", "attached assets", "release identification", "signing verification"
            )
        ) return null

        val icon = selectIcon(title, desc)
        return UserFacingReleaseNote(
            title = title,
            description = desc,
            icon = icon,
        )
    }

    private fun selectIcon(
        title: String,
        description: String,
    ): ImageVector {
        val combined = "$title $description".lowercase()
        return when {
            combined.contains("security") || combined.contains("protect") || combined.contains("verify") || combined.contains("safe") || combined.contains("shield") ->
                MetrolistIcons.Outlined.Security
            combined.contains("update") || combined.contains("download") || combined.contains("install") || combined.contains("release") ->
                MetrolistIcons.Outlined.Update
            combined.contains("upgrade") || combined.contains("sync") || combined.contains("restore") || combined.contains("preserve") || combined.contains("backup") ->
                MetrolistIcons.Filled.Sync
            combined.contains("audio") || combined.contains("equalizer") || combined.contains("eq") || combined.contains("sound") || combined.contains("volume") ->
                MetrolistIcons.Outlined.BarChart
            combined.contains("playlist") || combined.contains("artwork") || combined.contains("cover") || combined.contains("visual") || combined.contains("theme") ->
                MetrolistIcons.Filled.NewReleases
            else ->
                MetrolistIcons.Filled.Check
        }
    }
}
