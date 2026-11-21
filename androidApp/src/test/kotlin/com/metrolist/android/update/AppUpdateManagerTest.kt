package com.metrolist.android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class AppUpdateManagerTest {
    @Test
    fun officialReleaseApkSelection() {
        assertEquals(
            "DHUNLO-v1.0.1-release.apk",
            releaseWithVersion("v1.0.1", "DHUNLO-v1.0.1-release.apk").apk?.name,
        )
        assertEquals(
            "DHUNLO-1.0.1-release.apk",
            releaseWithVersion("v1.0.1", "DHUNLO-1.0.1-release.apk").apk?.name,
        )
        assertEquals(
            "DHUNLO-release.apk",
            releaseWithVersion("v1.0.1", "DHUNLO-release.apk").apk?.name,
        )
        // Official version-specific name takes precedence over legacy fallback
        assertEquals(
            "DHUNLO-v1.0.1-release.apk",
            releaseWithVersion("v1.0.1", "DHUNLO-v1.0.1-release.apk", "Dhunlo.apk").apk?.name,
        )
    }

    @Test
    fun legacyApkSelectionSupported() {
        assertEquals("Dhunlo.apk", releaseWithAssets("Dhunlo.apk").apk?.name)
        assertEquals("Dhunlo.apk", releaseWithAssets("Dhunlo.apk", "dhunlo-android-1.0.0-arm64-v8a.apk").apk?.name)
        assertEquals(
            "Dhunlo.apk",
            releaseWithAssets("Dhunlo-izzy.apk", "Dhunlo-with-Google-Cast.apk", "Dhunlo.apk").apk?.name,
        )
        assertEquals("dhunlo-android-1.0.0.apk", releaseWithAssets("dhunlo-android-1.0.0.apk").apk?.name)
        assertEquals("dhunlo-android-1.0.0-universal.apk", releaseWithAssets("dhunlo-android-1.0.0-universal.apk").apk?.name)
    }

    @Test
    fun wrongVersionAssetsRejected() {
        assertNull(releaseWithVersion("v1.0.1", "DHUNLO-v1.0.0-release.apk").apk)
        assertNull(releaseWithVersion("v1.0.1", "DHUNLO-1.0.0-release.apk").apk)
        assertNull(releaseWithVersion("v1.0.1", "dhunlo-android-0.9.0.apk").apk)
        assertNull(releaseWithVersion("v1.0.0", "dhunlo-android-0.9.0.apk").apk)
    }

    @Test
    fun ambiguousApkAssetsRejected() {
        // Case-insensitive duplicates
        assertNull(releaseWithAssets("Dhunlo.apk", "DHUNLO.APK").apk)
        assertNull(releaseWithVersion("v1.0.1", "DHUNLO-v1.0.1-release.apk", "dhunlo-v1.0.1-release.apk").apk)
        // Multiple conflicting versioned candidates
        assertNull(releaseWithVersion("v1.0.1", "DHUNLO-v1.0.1-release.apk", "DHUNLO-1.0.1-release.apk").apk)
    }

    @Test
    fun apkSelectionRejectsAbiSpecificIncompleteAndBlockedAssets() {
        assertNull(releaseWithAssets("dhunlo-android-1.0.0-arm64-v8a.apk").apk)
        assertNull(releaseWithAssets("dhunlo-android-1.0.0-debug.apk").apk)
        assertNull(releaseWithAssets("unknown.apk").apk)
        val incomplete = releaseWithAssets("Dhunlo.apk")
        assertNull(incomplete.copy(assets = incomplete.assets.map { it.copy(state = "new") }).apk)
    }

    @Test
    fun apkSelectionRejectsUpstreamMetrolistAssets() {
        assertNull(releaseWithAssets("Metrolist.apk").apk)
        assertNull(releaseWithAssets("metrolist-android-14.0.0.apk").apk)
        assertNull(releaseWithAssets("metrolist-android-14.0.0-universal.apk").apk)
    }

    @Test
    fun downloadUrlValidationOnlyAcceptsOfficialHttpsUrls() {
        // Valid official HTTPS URL
        assertTrue(
            AppUpdateManager.isValidAssetUrl(
                "https://github.com/avirallchaudhary/Dhunlo/releases/download/v1.0.1/DHUNLO-v1.0.1-release.apk",
            ),
        )

        // Insecure HTTP URL rejected
        assertFalse(
            AppUpdateManager.isValidAssetUrl(
                "http://github.com/avirallchaudhary/Dhunlo/releases/download/v1.0.1/DHUNLO-v1.0.1-release.apk",
            ),
        )

        // External/untrusted host rejected
        assertFalse(
            AppUpdateManager.isValidAssetUrl(
                "https://evil.com/avirallchaudhary/Dhunlo/releases/download/v1.0.1/DHUNLO-v1.0.1-release.apk",
            ),
        )

        // Wrong repository rejected
        assertFalse(
            AppUpdateManager.isValidAssetUrl(
                "https://github.com/otheruser/Dhunlo/releases/download/v1.0.1/DHUNLO-v1.0.1-release.apk",
            ),
        )

        // Null, empty, or malformed URLs rejected
        assertFalse(AppUpdateManager.isValidAssetUrl(null))
        assertFalse(AppUpdateManager.isValidAssetUrl(""))
        assertFalse(AppUpdateManager.isValidAssetUrl("not-a-valid-url"))

        // Release with invalid asset URL is rejected
        val badHostRelease =
            releaseWithUrl(
                "DHUNLO-v1.0.1-release.apk",
                "https://malicious.com/avirallchaudhary/Dhunlo/releases/download/v1.0.1/DHUNLO-v1.0.1-release.apk",
            )
        assertNull(badHostRelease.apk)

        val insecureRelease =
            releaseWithUrl(
                "DHUNLO-v1.0.1-release.apk",
                "http://github.com/avirallchaudhary/Dhunlo/releases/download/v1.0.1/DHUNLO-v1.0.1-release.apk",
            )
        assertNull(insecureRelease.apk)
    }

    @Test
    fun wrongDigestRejected() {
        val tempFile = File.createTempFile("dhunlo-digest-test", ".apk").apply {
            writeBytes("test APK byte stream".toByteArray())
            deleteOnExit()
        }
        val correctHash =
            MessageDigest.getInstance("SHA-256").digest(tempFile.readBytes()).joinToString("") { "%02x".format(it) }

        // Correct digest accepted
        assertTrue(AppUpdateManager.verifyDigest(tempFile, "sha256:$correctHash"))
        // Case-insensitive digest accepted
        assertTrue(AppUpdateManager.verifyDigest(tempFile, "sha256:${correctHash.uppercase()}"))

        // Wrong digest rejected
        assertFalse(
            AppUpdateManager.verifyDigest(
                tempFile,
                "sha256:0000000000000000000000000000000000000000000000000000000000000000",
            ),
        )

        // Null or malformed digest rejected
        assertFalse(AppUpdateManager.verifyDigest(tempFile, null))
        assertFalse(AppUpdateManager.verifyDigest(tempFile, ""))
        assertFalse(AppUpdateManager.verifyDigest(tempFile, "invalid_digest"))
        assertFalse(AppUpdateManager.verifyDigest(tempFile, "md5:abcd"))

        tempFile.delete()
    }

    @Test
    fun apkSignerMustMatchCurrentSignerOrProveForwardRotation() {
        val old = "old".encodeToByteArray()
        val current = "current".encodeToByteArray()
        val next = "next".encodeToByteArray()
        val trusted = setOf(AppUpdateManager.sha256(current))

        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(current), listOf(old, current), trusted))
        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(next), listOf(old, current, next), trusted))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(old), listOf(old), trusted))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(next), listOf(next), trusted))
        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(old, current), listOf(current, old), emptyList(), trusted))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(old, current), listOf(current), listOf(old, current), trusted))
        assertFalse(AppUpdateManager.signerDigestsMatch(emptyList(), listOf(current), listOf(current), trusted))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), emptyList(), listOf(current), trusted))
        assertFalse(AppUpdateManager.signerDigestsMatch(emptyList(), emptyList(), listOf(current), trusted))
    }

    @Test
    fun pinnedOfficialSignerValidation() {
        val officialSignerBytes = "official-signer-fixture".toByteArray()
        val officialFingerprint = AppUpdateManager.sha256(officialSignerBytes)
        val rogueSignerBytes = "rogue-signer-fixture".toByteArray()

        // Pinned signer matches
        assertTrue(
            AppUpdateManager.signerDigestsMatch(
                installedCurrentSigners = listOf(officialSignerBytes),
                archiveCurrentSigners = listOf(officialSignerBytes),
                trustedFingerprints = setOf(officialFingerprint),
            ),
        )

        // Rogue signer rejected even if installed matches archive
        assertFalse(
            AppUpdateManager.signerDigestsMatch(
                installedCurrentSigners = listOf(rogueSignerBytes),
                archiveCurrentSigners = listOf(rogueSignerBytes),
                trustedFingerprints = setOf(officialFingerprint),
            ),
        )
    }

    private fun releaseWithAssets(vararg names: String) =
        releaseWithVersion("v1.0.0", *names)

    private fun releaseWithVersion(tag: String, vararg names: String) =
        GitHubRelease(
            tagName = tag,
            htmlUrl = "https://github.com/avirallchaudhary/Dhunlo/releases/tag/$tag",
            assets =
                names.map {
                    GitHubReleaseAsset(
                        name = it,
                        state = "uploaded",
                        size = 100,
                        downloadUrl = "https://github.com/avirallchaudhary/Dhunlo/releases/download/$tag/$it",
                    )
                },
        )

    private fun releaseWithUrl(name: String, downloadUrl: String) =
        GitHubRelease(
            tagName = "v1.0.1",
            htmlUrl = "https://github.com/avirallchaudhary/Dhunlo/releases/tag/v1.0.1",
            assets =
                listOf(
                    GitHubReleaseAsset(
                        name = name,
                        state = "uploaded",
                        size = 100,
                        downloadUrl = downloadUrl,
                    ),
                ),
        )
}
