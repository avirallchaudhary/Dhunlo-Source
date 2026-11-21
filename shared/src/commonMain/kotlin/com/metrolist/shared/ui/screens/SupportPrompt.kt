/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.sponsor_developer
import com.metrolist.shared.generated.resources.support_prompt_message
import com.metrolist.shared.generated.resources.support_prompt_never_show
import com.metrolist.shared.generated.resources.support_prompt_title
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.AlertDialog
import com.metrolist.shared.ui.components.PopupVisibility
import com.metrolist.shared.ui.components.images.AsyncImage
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

private val SupportedDeveloperHandles = listOf("nyxiereal", "mostafaalagamy", "adrielGGmotion", "l6t9")
private val SupportPromptLaunchRange = 8..20

internal object SupportPromptSchedule {
    private var dueThisLaunch: Boolean? = null

    /** Counts one app launch per process and reports whether the prompt is due. Always false in DHUNLO. */
    fun claimLaunch(settings: Settings = Settings.shared): Boolean = false

    internal fun isDue(
        settings: Settings,
        random: Random = Random.Default,
    ): Boolean {
        if (settings.supportPromptDismissed) return false
        if (settings.supportPromptLaunchThreshold !in SupportPromptLaunchRange) {
            settings.supportPromptLaunchThreshold = SupportPromptLaunchRange.random(random)
        }
        val launches = settings.supportPromptLaunches + 1
        if (launches < settings.supportPromptLaunchThreshold) {
            settings.supportPromptLaunches = launches
            return false
        }
        // Dismissing without the button waits another random stretch of launches.
        settings.supportPromptLaunches = 0
        settings.supportPromptLaunchThreshold = SupportPromptLaunchRange.random(random)
        return true
    }
}

/** Disabled in DHUNLO: Upstream support prompt is eliminated completely. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SupportPrompt() {
    // Intentionally empty: DHUNLO does not show upstream support popups.
}
