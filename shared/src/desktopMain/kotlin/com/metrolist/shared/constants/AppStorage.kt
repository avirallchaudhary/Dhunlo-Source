/**
 * Dhunlo Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.constants

import com.metrolist.shared.utils.XdgUtils
import java.io.File
import java.nio.file.Path

object AppStorage {
    const val APP_ID = "dhunlo"
    const val APP_DIR_NAME = "dhunlo"
    const val DATABASE_NAME = "dhunlo.db"
    const val LOCK_FILE_NAME = "dhunlo.lock"
    const val RESTORE_PREFIX = "dhunlo.restore-"

    fun stateDir(): File = File(XdgUtils.getXdgStateHome(), APP_DIR_NAME)
    fun configDir(): File = File(XdgUtils.getXdgConfigHome(), APP_DIR_NAME)
    fun dataDir(): File = File(XdgUtils.getXdgDataHome(), APP_DIR_NAME)
    fun cacheDir(): File = File(XdgUtils.getXdgCacheHome(), APP_DIR_NAME)

    fun statePath(): Path = stateDir().toPath()
    fun configPath(): Path = configDir().toPath()
    fun dataPath(): Path = dataDir().toPath()
    fun cachePath(): Path = cacheDir().toPath()

    fun logsDir(): File = File(stateDir(), "logs")
    fun databaseFile(): File = File(dataDir(), DATABASE_NAME)
    fun historyDatabaseFile(): File = File(dataDir(), "history.db")
    fun crashesDir(): File = File(dataDir(), "crashes")
    fun downloadsDir(): File = File(dataDir(), "downloads")
    fun imageCacheDir(): File = File(cacheDir(), "image_cache")
    fun fontsDir(): File = File(dataDir(), "fonts")
    fun playlistCoversDir(): File = File(dataDir(), "playlist_covers")
}
