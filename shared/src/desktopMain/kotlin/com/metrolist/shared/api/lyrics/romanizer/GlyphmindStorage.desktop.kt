/*
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.api.lyrics.romanizer

import com.metrolist.shared.constants.AppStorage
import com.metrolist.shared.utils.XdgUtils
import java.io.File

internal actual fun glyphmindStorageDirectory(): String = File(AppStorage.dataDir(), "glyphmind").absolutePath

internal actual fun removeLegacyJapaneseDictionary() {
    File(AppStorage.dataDir(), "japanese-dictionary").deleteRecursively()
}
