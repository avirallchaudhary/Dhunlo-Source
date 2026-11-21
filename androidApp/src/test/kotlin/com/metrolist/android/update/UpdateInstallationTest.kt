package com.metrolist.android.update

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Looper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class UpdateInstallationTest {
    @After
    fun tearDown() {
        AppUpdateManager.trustedFingerprintsOverride = null
    }
    @Test
    fun packageVerificationRunsOffMainAfterPublishingInstallingStateAndFailureResetsIt() =
        runBlocking {
            val checked = AtomicBoolean(false)
            val context =
                object : ContextWrapper(RuntimeEnvironment.getApplication()) {
                    override fun getPackageManager(): PackageManager {
                        assertTrue(Looper.myLooper() != Looper.getMainLooper())
                        assertTrue((AppUpdateManager.state.value as AppUpdateState.Downloaded).installing)
                        checked.set(true)
                        return super.getPackageManager()
                    }
                }
            val file = File(context.cacheDir, "invalid-update.apk").apply { writeText("invalid APK") }
            val downloaded = AppUpdateState.Downloaded(GitHubRelease("test", htmlUrl = ""), file)
            try {
                assertFalse(AppUpdateManager.install(context, downloaded))
                assertTrue(checked.get())
                assertEquals(AppUpdateState.Available(downloaded.release), AppUpdateManager.state.value)
                assertFalse(file.exists())
            } finally {
                file.delete()
            }
        }

    @Test
    fun missingPackageOffersAnotherDownload() =
        runBlocking {
            val context = RuntimeEnvironment.getApplication()
            val file = File(context.cacheDir, "missing-update.apk")
            file.delete()
            val release = GitHubRelease("test", htmlUrl = "")

            assertFalse(AppUpdateManager.install(context, AppUpdateState.Downloaded(release, file)))
            assertEquals(AppUpdateState.Available(release), AppUpdateManager.state.value)
            AppUpdateManager.download(context, release, crashRecovery = true)
            assertEquals(AppUpdateState.Error(UpdateFailure.NO_APK, release), AppUpdateManager.state.value)
        }

    @Test
    @Config(sdk = [27])
    fun missingDownloadedPackageReturnsToAvailableOnCheck() =
        runBlocking {
            val context = RuntimeEnvironment.getApplication()
            val downloaded = stageVerifiedPackage(context)
            downloaded.file.delete()

            assertEquals(downloaded.release, AppUpdateManager.check(context, crashRecovery = true))
            assertEquals(AppUpdateState.Available(downloaded.release), AppUpdateManager.state.value)
        }

    @Test
    @Config(sdk = [27])
    fun missingDownloadedPackageDoesNotBlockAnotherDownloadAttempt() =
        runBlocking {
            val context = RuntimeEnvironment.getApplication()
            val downloaded = stageVerifiedPackage(context)
            downloaded.file.delete()

            AppUpdateManager.download(context, downloaded.release, crashRecovery = true)
            assertEquals(AppUpdateState.Error(UpdateFailure.NO_APK, downloaded.release), AppUpdateManager.state.value)
        }

    @Test
    fun packageIdMismatchRejected() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.cacheDir, "pkg-mismatch.apk").apply { writeText("APK fixture") }
        val signer = Signature(byteArrayOf(1, 2, 3))
        AppUpdateManager.trustedFingerprintsOverride = setOf(AppUpdateManager.sha256(signer.toByteArray()))
        val packageManager = shadowOf(context.packageManager)
        packageManager.getInternalMutablePackageInfo(context.packageName).apply {
            signatures = arrayOf(signer)
            versionCode = 1
        }
        packageManager.setPackageArchiveInfo(
            file.absolutePath,
            PackageInfo().apply {
                packageName = "com.other.malicious"
                signatures = arrayOf(signer)
                versionCode = 2
            },
        )
        try {
            assertEquals(ApkIdentityResult.PACKAGE_MISMATCH, AppUpdateManager.checkApkIdentity(context, file))
            assertFalse(AppUpdateManager.verifyApkIdentity(context, file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun lowerOrEqualVersionCodeRejected() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.cacheDir, "version-code.apk").apply { writeText("APK fixture") }
        val signer = Signature(byteArrayOf(1, 2, 3))
        AppUpdateManager.trustedFingerprintsOverride = setOf(AppUpdateManager.sha256(signer.toByteArray()))
        val packageManager = shadowOf(context.packageManager)
        packageManager.getInternalMutablePackageInfo(context.packageName).apply {
            signatures = arrayOf(signer)
            versionCode = 100
        }
        try {
            // Equal versionCode
            packageManager.setPackageArchiveInfo(
                file.absolutePath,
                PackageInfo().apply {
                    packageName = context.packageName
                    signatures = arrayOf(signer)
                    versionCode = 100
                },
            )
            assertEquals(ApkIdentityResult.DOWNGRADE, AppUpdateManager.checkApkIdentity(context, file))
            assertFalse(AppUpdateManager.verifyApkIdentity(context, file))

            // Lower versionCode
            packageManager.setPackageArchiveInfo(
                file.absolutePath,
                PackageInfo().apply {
                    packageName = context.packageName
                    signatures = arrayOf(signer)
                    versionCode = 99
                },
            )
            assertEquals(ApkIdentityResult.DOWNGRADE, AppUpdateManager.checkApkIdentity(context, file))
            assertFalse(AppUpdateManager.verifyApkIdentity(context, file))

            // Higher versionCode
            packageManager.setPackageArchiveInfo(
                file.absolutePath,
                PackageInfo().apply {
                    packageName = context.packageName
                    signatures = arrayOf(signer)
                    versionCode = 101
                },
            )
            assertEquals(ApkIdentityResult.VALID, AppUpdateManager.checkApkIdentity(context, file))
            assertTrue(AppUpdateManager.verifyApkIdentity(context, file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun pinnedSignerMismatchRejected() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.cacheDir, "signer-mismatch.apk").apply { writeText("APK fixture") }
        val mockSigner = Signature(byteArrayOf(9, 9, 9))
        // Do not override trustedFingerprintsOverride; use default OFFICIAL_SIGNER_SHA256
        AppUpdateManager.trustedFingerprintsOverride = null
        val packageManager = shadowOf(context.packageManager)
        packageManager.getInternalMutablePackageInfo(context.packageName).apply {
            signatures = arrayOf(mockSigner)
            versionCode = 1
        }
        packageManager.setPackageArchiveInfo(
            file.absolutePath,
            PackageInfo().apply {
                packageName = context.packageName
                signatures = arrayOf(mockSigner)
                versionCode = 2
            },
        )
        try {
            assertEquals(ApkIdentityResult.SIGNER_MISMATCH, AppUpdateManager.checkApkIdentity(context, file))
            assertFalse(AppUpdateManager.verifyApkIdentity(context, file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun correctSignerAccepted() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.cacheDir, "signer-correct.apk").apply { writeText("APK fixture") }
        val officialSignerBytes = byteArrayOf(4, 5, 6)
        val officialFingerprint = AppUpdateManager.sha256(officialSignerBytes)
        AppUpdateManager.trustedFingerprintsOverride = setOf(officialFingerprint)
        val officialSigner = Signature(officialSignerBytes)
        val packageManager = shadowOf(context.packageManager)
        packageManager.getInternalMutablePackageInfo(context.packageName).apply {
            signatures = arrayOf(officialSigner)
            versionCode = 156
        }
        packageManager.setPackageArchiveInfo(
            file.absolutePath,
            PackageInfo().apply {
                packageName = context.packageName
                signatures = arrayOf(officialSigner)
                versionCode = 157
            },
        )
        try {
            assertEquals(ApkIdentityResult.VALID, AppUpdateManager.checkApkIdentity(context, file))
            assertTrue(AppUpdateManager.verifyApkIdentity(context, file))
        } finally {
            file.delete()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun stageVerifiedPackage(context: Context): AppUpdateState.Downloaded {
        val file = File(context.cacheDir, "verified-update.apk").apply { writeText("APK fixture") }
        val signer = Signature(byteArrayOf(1, 2, 3))
        AppUpdateManager.trustedFingerprintsOverride = setOf(AppUpdateManager.sha256(signer.toByteArray()))
        val packageManager = shadowOf(context.packageManager)
        packageManager.getInternalMutablePackageInfo(context.packageName).apply {
            signatures = arrayOf(signer)
            versionCode = 1
        }
        packageManager.setPackageArchiveInfo(
            file.absolutePath,
            PackageInfo().apply {
                packageName = context.packageName
                signatures = arrayOf(signer)
                versionCode = 2
            },
        )
        val downloaded = AppUpdateState.Downloaded(GitHubRelease("test", htmlUrl = ""), file)
        assertTrue(AppUpdateManager.install(context, downloaded, crashRecovery = true))
        AppUpdateManager.onInstallAborted()
        assertEquals(downloaded, AppUpdateManager.state.value)
        assertTrue(file.isFile)
        return downloaded
    }
}
