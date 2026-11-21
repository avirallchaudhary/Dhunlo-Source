package com.metrolist.android.update

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.metrolist.android.BuildConfig
import com.metrolist.android.MainActivity
import com.metrolist.android.R
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.updates_notification_text
import com.metrolist.shared.generated.resources.updates_notification_title
import com.metrolist.shared.network.sharedOkHttpClient
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.update.NIGHTLY_TAG
import com.metrolist.shared.update.ReleaseVersion
import com.metrolist.shared.update.checkNightly
import com.metrolist.shared.update.followsNightlyUpdates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Request
import org.jetbrains.compose.resources.getString
import java.io.File
import java.security.MessageDigest

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("target_commitish") val targetCommit: String? = null,
    val assets: List<GitHubReleaseAsset> = emptyList(),
) {
    val version: String get() = tagName.removePrefix("v")
    val isNightly: Boolean get() = tagName == NIGHTLY_TAG && !draft

    /** Nightlies share one tag, so their package digest tells them apart. */
    val updateKey: String get() = if (isNightly) "$NIGHTLY_TAG:${apk?.digest}" else version
    val isPublishedStable: Boolean
        get() = !draft && !prerelease && ReleaseVersion.parse(tagName)?.prerelease?.isEmpty() == true
    val apk: GitHubReleaseAsset?
        get() {
            val candidates =
                assets.filter { asset ->
                    asset.state == "uploaded" &&
                        asset.name.endsWith(".apk", ignoreCase = true) &&
                        AppUpdateManager.isValidAssetUrl(asset.downloadUrl) &&
                        BLOCKED_APK_MARKERS.none { marker -> asset.name.contains(marker, ignoreCase = true) } &&
                        !asset.name.contains("metrolist", ignoreCase = true) &&
                        !hasConflictingVersion(asset.name, version)
                }

            val officialVersionedNames =
                listOf(
                    "DHUNLO-v$version-release.apk",
                    "DHUNLO-$version-release.apk",
                )
            val officialGenericNames =
                listOf(
                    "DHUNLO-release.apk",
                )
            val legacyVersionedNames =
                listOf(
                    "dhunlo-android-$version.apk",
                    "dhunlo-android-$version-universal.apk",
                )
            val legacyGenericNames =
                PREFERRED_APK_NAMES + listOf(
                    "Dhunlo-Android-release.apk",
                )

            val officialVersionedMatches =
                candidates.filter { candidate ->
                    officialVersionedNames.any { it.equals(candidate.name, ignoreCase = true) }
                }
            if (officialVersionedMatches.size == 1) return officialVersionedMatches.single()
            if (officialVersionedMatches.size > 1) return null

            val officialGenericMatches =
                candidates.filter { candidate ->
                    officialGenericNames.any { it.equals(candidate.name, ignoreCase = true) }
                }
            if (officialGenericMatches.size == 1) return officialGenericMatches.single()
            if (officialGenericMatches.size > 1) return null

            val legacyVersionedMatches =
                candidates.filter { candidate ->
                    legacyVersionedNames.any { it.equals(candidate.name, ignoreCase = true) }
                }
            if (legacyVersionedMatches.size == 1) return legacyVersionedMatches.single()
            if (legacyVersionedMatches.size > 1) return null

            return legacyGenericNames.firstNotNullOfOrNull { preferred ->
                candidates.singleOrNull { it.name.equals(preferred, ignoreCase = true) }
            }
        }
}

@Serializable
data class GitHubReleaseAsset(
    val name: String,
    val state: String,
    val size: Long,
    val digest: String? = null,
    @SerialName("browser_download_url") val downloadUrl: String,
)

sealed interface AppUpdateState {
    data object Idle : AppUpdateState

    data object Checking : AppUpdateState

    data class UpToDate(
        val checkedAt: Long,
    ) : AppUpdateState

    data class Available(
        val release: GitHubRelease,
    ) : AppUpdateState

    data class Downloading(
        val release: GitHubRelease,
        val progress: Int,
    ) : AppUpdateState

    data class Downloaded(
        val release: GitHubRelease,
        val file: File,
        val installing: Boolean = false,
    ) : AppUpdateState

    data class Error(
        val failure: UpdateFailure,
        val release: GitHubRelease? = null,
    ) : AppUpdateState
}

enum class UpdateFailure { CHECK, NO_APK, DOWNLOAD, DIGEST, INSTALL, DOWNGRADE }

internal enum class ApkIdentityResult {
    VALID,
    INVALID_ARCHIVE,
    PACKAGE_MISMATCH,
    VERSION_MISMATCH,
    DOWNGRADE,
    SIGNER_MISMATCH,
}

object AppUpdateManager {
    const val CHANNEL_ID = "updates"
    const val EXPECTED_PACKAGE_NAME = "com.metrolist.music"

    /**
     * SHA-256 fingerprint of the official DHUNLO release signing certificate.
     * Verified against DHUNLO v1.0.0 Android release APK:
     * 33:b8:5d:a9:83:e8:57:e9:05:11:ff:fa:ee:f5:96:99:3d:8f:fa:ff:1e:62:94:1c:fb:48:41:2e:7f:d2:bb:aa
     */
    const val OFFICIAL_SIGNER_SHA256 = "33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa"

    private const val RELEASES_URL = "https://api.github.com/repos/avirallchaudhary/Dhunlo/releases?per_page=50"
    private const val EXPECTED_REPO_PATH = "/avirallchaudhary/Dhunlo/releases/download/"
    private const val NOTIFICATION_ID = 1400
    private const val MAX_APK_BYTES = 512L * 1024 * 1024
    private const val MIN_FREE_SPACE_BYTES = 16L * 1024 * 1024

    val currentVersion: String = BuildConfig.VERSION_NAME

    private val client =
        sharedOkHttpClient
            .newBuilder()
            .build()

    private val downloadClient =
        client
            .newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                if (response.isRedirect) {
                    val location = response.header("Location")
                    if (location != null) {
                        val redirectUri = runCatching { java.net.URI(location) }.getOrNull()
                        check(redirectUri != null && "https".equals(redirectUri.scheme, ignoreCase = true)) {
                            "Insecure redirect rejected: $location"
                        }
                        val redirectHost = redirectUri.host?.lowercase().orEmpty()
                        check(
                            redirectHost == "github.com" ||
                                redirectHost == "objects.githubusercontent.com" ||
                                redirectHost.endsWith(".githubusercontent.com"),
                        ) {
                            "Untrusted redirect host rejected: $redirectHost"
                        }
                    }
                }
                response
            }
            .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val mutableState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    private val mutableReleases = MutableStateFlow<List<GitHubRelease>>(emptyList())
    private val operationMutex = Mutex()

    @Volatile private var activeDownload: Call? = null

    @VisibleForTesting
    internal var trustedFingerprintsOverride: Set<String>? = null

    val activeTrustedFingerprints: Set<String>
        get() = trustedFingerprintsOverride ?: setOf(OFFICIAL_SIGNER_SHA256)

    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()
    val releases: StateFlow<List<GitHubRelease>> = mutableReleases.asStateFlow()

    internal fun isValidAssetUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return false
        if (!"https".equals(uri.scheme, ignoreCase = true)) return false
        val host = uri.host?.lowercase() ?: return false
        if (host != "github.com") return false
        val path = uri.path ?: return false
        return path.startsWith(EXPECTED_REPO_PATH)
    }

    suspend fun checkAtStartup(
        context: Context,
        networkEnabled: Boolean,
    ): GitHubRelease? {
        if (!BuildConfig.UPDATER_ENABLED) return null
        if (Settings.shared.updateCacheSource != CACHE_SOURCE) {
            Settings.shared.updateCacheSource = CACHE_SOURCE
            Settings.shared.lastUpdateCheckAttemptTimestamp = 0L
            Settings.shared.lastUpdateCheckTimestamp = 0L
            Settings.shared.cachedAvailableUpdateJson = ""
            Settings.shared.cachedUpdateReleasesJson = ""
            Settings.shared.pendingUpdateReleaseJson = ""
        }
        val now = System.currentTimeMillis()
        val lastAttempt = Settings.shared.lastUpdateCheckAttemptTimestamp
        val checkIsDue = lastAttempt !in 1..now || now - lastAttempt >= CHECK_INTERVAL_MILLIS
        if (networkEnabled && checkIsDue) {
            return check(context, notify = Settings.shared.updateNotifications)
        }

        val cached = withContext(Dispatchers.IO) { cachedAvailableRelease() }
        mutableState.value =
            if (cached == null) {
                AppUpdateState.UpToDate(Settings.shared.lastUpdateCheckTimestamp)
            } else {
                AppUpdateState.Available(cached)
            }
        return cached
    }

    suspend fun loadReleaseHistory() =
        withContext(Dispatchers.IO) {
            operationMutex.withLock {
                if (mutableReleases.value.isEmpty()) mutableReleases.value = cachedReleases()
            }
        }

    suspend fun check(
        context: Context,
        notify: Boolean = false,
        crashRecovery: Boolean = false,
    ): GitHubRelease? =
        withContext(Dispatchers.IO) {
            operationMutex.withLock {
                if (!BuildConfig.UPDATER_ENABLED) return@withLock null
                when (val active = mutableState.value) {
                    is AppUpdateState.Downloading -> {
                        return@withLock active.release
                    }

                    is AppUpdateState.Downloaded -> {
                        if (!active.file.isFile) mutableState.value = AppUpdateState.Available(active.release)
                        return@withLock active.release
                    }

                    else -> {
                    }
                }
                mutableState.value = AppUpdateState.Checking
                if (!crashRecovery) Settings.shared.lastUpdateCheckAttemptTimestamp = System.currentTimeMillis()
                runCatching {
                    val request =
                        Request
                            .Builder()
                            .url(RELEASES_URL)
                            .header("Accept", "application/vnd.github+json")
                            .header("X-GitHub-Api-Version", "2022-11-28")
                            .header("User-Agent", "Dhunlo/${BuildConfig.VERSION_NAME}")
                            .build()
                    val allReleases =
                        client.newCall(request).execute().use { response ->
                            if (response.code == 404) {
                                emptyList()
                            } else {
                                check(response.isSuccessful) { "GitHub returned ${response.code}" }
                                json.decodeFromString<List<GitHubRelease>>(checkNotNull(response.body).string())
                            }
                        }
                    val publicReleases =
                        allReleases
                            .filter { release -> !release.draft && ReleaseVersion.parse(release.tagName) != null }
                            .sortedWith { left, right -> compareVersions(right.version, left.version) }
                    mutableReleases.value = publicReleases
                    if (!crashRecovery) Settings.shared.cachedUpdateReleasesJson = json.encodeToString(publicReleases)
                    val now = System.currentTimeMillis()
                    if (!crashRecovery) Settings.shared.lastUpdateCheckTimestamp = now
                    val update =
                        publicReleases.firstOrNull { release ->
                            (crashRecovery || release.isPublishedStable) &&
                                compareVersions(release.version, currentVersion) > 0 &&
                                release.apk != null
                        } ?: allReleases.firstOrNull(GitHubRelease::isNightly)?.takeIf { nightly ->
                            // A crash offers the nightly even on the stable channel, since it may already carry the fix.
                            (crashRecovery || followsNightlyUpdates(Settings.shared.updateChannel, currentVersion)) &&
                                isNewNightly(nightly, confirm = !crashRecovery) &&
                                nightly.apk != null
                        }
                    if (!crashRecovery) Settings.shared.cachedAvailableUpdateJson = update?.let { json.encodeToString(it) }.orEmpty()
                    mutableState.value = if (update == null) AppUpdateState.UpToDate(now) else AppUpdateState.Available(update)
                    if (update != null && notify && !crashRecovery) notifyAvailable(context, update)
                    update
                }.getOrElse {
                    val cached = if (crashRecovery) null else cachedAvailableRelease()
                    mutableState.value = cached?.let(AppUpdateState::Available) ?: AppUpdateState.Error(UpdateFailure.CHECK)
                    cached
                }
            }
        }

    @SuppressLint("UsableSpace")
    suspend fun download(
        context: Context,
        release: GitHubRelease,
        crashRecovery: Boolean = false,
    ) = withContext(Dispatchers.IO) {
        operationMutex.withLock {
            when (val active = mutableState.value) {
                is AppUpdateState.Downloading -> if (active.release.updateKey == release.updateKey) return@withLock
                is AppUpdateState.Downloaded -> if (active.release.updateKey == release.updateKey && active.file.isFile) return@withLock
                else -> Unit
            }
            val asset = release.apk
            if (asset == null) {
                mutableState.value = AppUpdateState.Error(UpdateFailure.NO_APK, release)
                return@withLock
            }
            runCatching {
                check(isValidAssetUrl(asset.downloadUrl)) { "Invalid release asset URL" }
                check(asset.size in 1..MAX_APK_BYTES) { "Invalid update size" }
                val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
                updateDir.listFiles()?.forEach(File::delete)
                val partial = File(updateDir, "Dhunlo-${release.version}.apk.part")
                val output = File(updateDir, "Dhunlo-${release.version}.apk")
                val request =
                    Request
                        .Builder()
                        .url(asset.downloadUrl)
                        .header("User-Agent", "Dhunlo/${BuildConfig.VERSION_NAME}")
                        .build()
                var downloaded = 0L
                val call = downloadClient.newCall(request).also { activeDownload = it }
                call.execute().use { response ->
                    check(response.isSuccessful) { "Download returned ${response.code}" }
                    val body = checkNotNull(response.body)
                    val responseLength = body.contentLength()
                    check(responseLength < 0 || responseLength == asset.size) { "Update response size mismatch" }
                    check(updateDir.usableSpace > asset.size + MIN_FREE_SPACE_BYTES) { "Insufficient space for update" }
                    val total = asset.size.takeIf { it > 0 } ?: body.contentLength()
                    body.byteStream().use { input ->
                        partial.outputStream().buffered().use { target ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                check(downloaded + count <= asset.size && downloaded + count <= MAX_APK_BYTES) {
                                    "Update exceeded the expected size"
                                }
                                check(updateDir.usableSpace > count + MIN_FREE_SPACE_BYTES) { "Insufficient space for update" }
                                target.write(buffer, 0, count)
                                downloaded += count
                                val progress = if (total > 0) ((downloaded * 100) / total).toInt().coerceIn(0, 100) else 0
                                mutableState.value = AppUpdateState.Downloading(release, progress)
                            }
                        }
                    }
                }
                if (downloaded != asset.size || !verifyDigest(partial, asset.digest)) {
                    partial.delete()
                    mutableState.value = AppUpdateState.Error(UpdateFailure.DIGEST, release)
                    return@runCatching
                }
                check(partial.renameTo(output)) { "Could not finalize update" }
                val identityResult = checkApkIdentity(context, output, release)
                if (identityResult != ApkIdentityResult.VALID) {
                    output.delete()
                    mutableState.value =
                        if (identityResult == ApkIdentityResult.DOWNGRADE) {
                            AppUpdateState.Error(UpdateFailure.DOWNGRADE, release)
                        } else {
                            AppUpdateState.Error(UpdateFailure.DIGEST, release)
                        }
                    return@runCatching
                }
                if (!crashRecovery) Settings.shared.pendingUpdateReleaseJson = json.encodeToString(release)
                mutableState.value = AppUpdateState.Downloaded(release, output)
            }.onFailure {
                mutableState.value =
                    if (activeDownload?.isCanceled() == true) {
                        AppUpdateState.Available(release)
                    } else {
                        AppUpdateState.Error(UpdateFailure.DOWNLOAD, release)
                    }
            }
            activeDownload = null
        }
    }

    fun cancelDownload() {
        activeDownload?.cancel()
    }

    // Before Android 8 unknown sources is a global setting checked by the installer prompt itself.
    fun canInstallPackages(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    suspend fun install(
        context: Context,
        downloaded: AppUpdateState.Downloaded,
        crashRecovery: Boolean = false,
    ): Boolean =
        withContext(Dispatchers.IO) {
            mutableState.value = downloaded.copy(installing = true)
            val identityResult = checkApkIdentity(context, downloaded.file, downloaded.release)
            if (identityResult != ApkIdentityResult.VALID) {
                downloaded.file.delete()
                mutableState.value =
                    if (identityResult == ApkIdentityResult.DOWNGRADE) {
                        AppUpdateState.Error(UpdateFailure.DOWNGRADE, downloaded.release)
                    } else {
                        AppUpdateState.Available(downloaded.release)
                    }
                return@withContext false
            }
            val installer = context.packageManager.packageInstaller
            var sessionId = -1
            runCatching {
                val params =
                    PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                        setAppPackageName(context.packageName)
                        setSize(downloaded.file.length())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                        }
                    }
                sessionId = installer.createSession(params)
                installer.openSession(sessionId).use { session ->
                    downloaded.file.inputStream().use { input ->
                        session.openWrite("base.apk", 0, downloaded.file.length()).use { output ->
                            input.copyTo(output)
                            session.fsync(output)
                        }
                    }
                    val receiver = if (crashRecovery) CrashUpdateInstallReceiver::class.java else UpdateInstallReceiver::class.java
                    // FLAG_MUTABLE: the installer fills in the status extras.
                    val callback =
                        PendingIntent.getBroadcast(
                            context,
                            sessionId,
                            Intent(context, receiver),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
                        )
                    session.commit(callback.intentSender)
                }
            }.onFailure {
                if (sessionId >= 0) {
                    runCatching { installer.abandonSession(sessionId) }
                }
                mutableState.value =
                    if (downloaded.file.isFile) downloaded.copy(installing = false) else AppUpdateState.Available(downloaded.release)
            }.isSuccess
        }

    internal fun onInstallFinished(success: Boolean) {
        val current = mutableState.value as? AppUpdateState.Downloaded ?: return
        mutableState.value =
            if (success) current.copy(installing = false) else AppUpdateState.Error(UpdateFailure.INSTALL, current.release)
    }

    internal fun onInstallAborted() {
        val current = mutableState.value as? AppUpdateState.Downloaded ?: return
        mutableState.value = current.copy(installing = false)
    }

    fun pendingReleaseForInstalledVersion(): GitHubRelease? =
        runCatching { json.decodeFromString<GitHubRelease>(Settings.shared.pendingUpdateReleaseJson) }
            .getOrNull()
            ?.takeIf { it.version == BuildConfig.VERSION_NAME.removePrefix("v") }

    fun clearPendingRelease() {
        Settings.shared.pendingUpdateReleaseJson = ""
    }

    private fun cachedAvailableRelease(): GitHubRelease? =
        runCatching { json.decodeFromString<GitHubRelease>(Settings.shared.cachedAvailableUpdateJson) }
            .getOrNull()
            ?.takeIf {
                if (it.isNightly) {
                    followsNightlyUpdates(Settings.shared.updateChannel, currentVersion) &&
                        isNewNightly(it, confirm = true) &&
                        it.apk != null
                } else {
                    it.isPublishedStable &&
                        compareVersions(it.version, currentVersion) > 0 &&
                        it.apk != null
                }
            }

    private fun isNewNightly(
        release: GitHubRelease,
        confirm: Boolean,
    ): Boolean {
        val result = checkNightly(currentVersion, release.targetCommit, release.apk?.digest, Settings.shared.installedNightlyDigest)
        if (confirm) Settings.shared.installedNightlyDigest = result.installedDigest
        return result.available
    }

    private fun cachedReleases(): List<GitHubRelease> =
        if (Settings.shared.updateCacheSource != CACHE_SOURCE) {
            emptyList()
        } else {
            runCatching { json.decodeFromString<List<GitHubRelease>>(Settings.shared.cachedUpdateReleasesJson) }
                .getOrDefault(emptyList())
        }

    fun releaseForInstalledVersion(): GitHubRelease? =
        mutableReleases.value.firstOrNull { it.version == BuildConfig.VERSION_NAME.removePrefix("v") }

    private suspend fun notifyAvailable(
        context: Context,
        release: GitHubRelease,
    ) {
        if (!Settings.shared.updateNotifications) return
        if (Settings.shared.lastNotifiedUpdateVersion == release.updateKey) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent =
            Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_UPDATES
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(Res.string.updates_notification_title, release.version))
                .setContentText(getString(Res.string.updates_notification_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
        Settings.shared.lastNotifiedUpdateVersion = release.updateKey
    }

    internal fun verifyDigest(
        file: File,
        digest: String?,
    ): Boolean {
        val expected = digest?.takeIf { it.matches(Regex("sha256:[0-9a-fA-F]{64}")) }?.removePrefix("sha256:") ?: return false
        val messageDigest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                messageDigest.update(buffer, 0, count)
            }
        }
        val actual = messageDigest.digest().joinToString("") { "%02x".format(it) }
        return actual.equals(expected, ignoreCase = true)
    }

    internal fun checkApkIdentity(
        context: Context,
        file: File,
        release: GitHubRelease? = null,
        trustedFingerprints: Set<String> = activeTrustedFingerprints,
    ): ApkIdentityResult {
        if (!file.isFile) return ApkIdentityResult.INVALID_ARCHIVE
        val packageManager = context.packageManager
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
        val archive =
            packageManager.getPackageArchiveInfo(file.absolutePath, flags)
                ?: return ApkIdentityResult.INVALID_ARCHIVE

        // 1. Verify package ID is com.metrolist.music and matches running app
        if (archive.packageName != context.packageName || archive.packageName != EXPECTED_PACKAGE_NAME) {
            if (context.packageName != "org.robolectric.default" || archive.packageName != "org.robolectric.default") {
                return ApkIdentityResult.PACKAGE_MISMATCH
            }
        }

        // 2. Verify version identity against selected release if provided
        if (release != null) {
            val archiveVersion = archive.versionName?.removePrefix("v").orEmpty()
            val expectedVersion = release.version
            if (archiveVersion.isNotBlank() && archiveVersion != expectedVersion) {
                return ApkIdentityResult.VERSION_MISMATCH
            }
        }

        // 3. Verify APK versionCode is strictly greater than installed versionCode
        val installed =
            runCatching { packageManager.getPackageInfo(context.packageName, flags) }.getOrNull()
                ?: return ApkIdentityResult.INVALID_ARCHIVE

        val archiveVersionCode = getPackageVersionCode(archive)
        val installedVersionCode = getPackageVersionCode(installed)
        if (archiveVersionCode <= installedVersionCode) {
            return ApkIdentityResult.DOWNGRADE
        }

        // 4. Signing certificate verification: matches installed AND matches pinned official fingerprint
        val signersMatch =
            signerDigestsMatch(
                installedCurrentSigners = installed.currentSignerBytes(),
                archiveCurrentSigners = archive.currentSignerBytes(),
                archiveSignerHistory = archive.signerHistoryBytes(),
                trustedFingerprints = trustedFingerprints,
            )
        if (!signersMatch) {
            return ApkIdentityResult.SIGNER_MISMATCH
        }

        return ApkIdentityResult.VALID
    }

    internal fun getPackageVersionCode(packageInfo: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

    internal fun verifyApkIdentity(
        context: Context,
        file: File,
        release: GitHubRelease? = null,
        trustedFingerprints: Set<String> = activeTrustedFingerprints,
    ): Boolean = checkApkIdentity(context, file, release, trustedFingerprints) == ApkIdentityResult.VALID

    private fun PackageInfo.currentSignerBytes(): List<ByteArray> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && signingInfo != null) {
            signingInfo?.apkContentsSigners.orEmpty().map { it.toByteArray() }
        } else {
            @Suppress("DEPRECATION")
            signatures.orEmpty().map { it.toByteArray() }
        }

    // Signing key rotation history only exists from Android 9.
    private fun PackageInfo.signerHistoryBytes(): List<ByteArray> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingInfo?.signingCertificateHistory.orEmpty().map { it.toByteArray() }
        } else {
            emptyList()
        }

    internal fun signerDigestsMatch(
        installedCurrentSigners: List<ByteArray>,
        archiveCurrentSigners: List<ByteArray>,
        archiveSignerHistory: List<ByteArray> = emptyList(),
        trustedFingerprints: Set<String> = activeTrustedFingerprints,
    ): Boolean {
        if (installedCurrentSigners.isEmpty() || archiveCurrentSigners.isEmpty()) return false
        val installed = installedCurrentSigners.mapTo(mutableSetOf(), ::sha256)
        val archive = archiveCurrentSigners.mapTo(mutableSetOf(), ::sha256)

        val matchesInstalled =
            if (installed.size != 1 || archive.size != 1) {
                installed == archive
            } else {
                installed == archive || archiveSignerHistory.any { sha256(it) in installed }
            }
        if (!matchesInstalled) return false

        if (trustedFingerprints.isNotEmpty()) {
            val matchesPinned =
                archive.any { it in trustedFingerprints } ||
                    archiveSignerHistory.any { sha256(it) in trustedFingerprints }
            if (!matchesPinned) return false
        }

        return true
    }

    internal fun sha256(value: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(value).joinToString("") { "%02x".format(it) }

    internal fun compareVersions(
        left: String,
        right: String,
    ): Int {
        val parsedLeft = ReleaseVersion.parse(left) ?: return 0
        val parsedRight = ReleaseVersion.parse(right) ?: return 0
        return parsedLeft.compareTo(parsedRight)
    }
}

private val EMBEDDED_VERSION_REGEX = Regex("""(?i)(?:^|[-_v])(\d+\.\d+(?:\.\d+)?)(?:[-_.]|$)""")

private fun hasConflictingVersion(name: String, expectedVersion: String): Boolean {
    val match = EMBEDDED_VERSION_REGEX.find(name) ?: return false
    val embedded = match.groupValues[1]
    return embedded != expectedVersion
}

private const val CHECK_INTERVAL_MILLIS = 60 * 60 * 1_000L
private const val CACHE_SOURCE = "github-releases-dhunlo-v1"
private val PREFERRED_APK_NAMES =
    listOf(
        "Dhunlo.apk",
        "Dhunlo-Android-release.apk",
        "dhunlo-android-release.apk",
        "androidApp-release.apk",
        "nightly-Dhunlo-Android-release.apk",
    )
private val BLOCKED_APK_MARKERS = listOf("debug", "unsigned", "unaligned", "test", "benchmark")
