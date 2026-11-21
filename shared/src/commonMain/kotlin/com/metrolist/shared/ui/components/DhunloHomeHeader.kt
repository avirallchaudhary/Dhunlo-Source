package com.metrolist.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.design.glass.DhunloBrandMark
import com.metrolist.shared.ui.design.glass.DhunloBranding
import com.metrolist.shared.ui.design.glass.DhunloGlassTokens
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Returns a time-appropriate greeting for the current hour:
 * 04:00 - 11:59 -> "Good morning"
 * 12:00 - 16:59 -> "Good afternoon"
 * 17:00 - 21:59 -> "Good evening"
 * 22:00 - 03:59 -> "Good night"
 */
fun getTimeBasedGreeting(hour: Int = currentHourOfDay()): String =
    when (hour) {
        in 4..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

fun currentHourOfDay(): Int =
    try {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    } catch (_: Exception) {
        12
    }

/**
 * Formats the greeting text, appending the user's display name if signed in.
 */
fun formatHomeGreeting(accountName: String?, hour: Int = currentHourOfDay()): String {
    val greeting = getTimeBasedGreeting(hour)
    val name = accountName?.trim()?.takeIf { it.isNotBlank() }
    return if (name != null) "$greeting, $name" else greeting
}

/**
 * Premium Home Screen Header featuring:
 * - Dhunlo brand badge ("DHUNLO • Pure Audio Experience")
 * - Personalized, time-aware greeting ("Good morning", "Good afternoon", etc.)
 * - Supporting musical subtitle ("Discover today's soundtrack")
 * - Dhunlo circular brand mark with specular border ring
 */
@Composable
fun DhunloHomeHeader(
    accountName: String? = null,
    isWide: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val isDark = Settings.shared.getCurrentThemeIsDark()
    val greeting = remember(accountName) { formatHomeGreeting(accountName) }
    val horizontalPadding = if (isWide) 24.dp else 16.dp

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = 1400.dp)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = if (isWide) 16.dp else 10.dp, bottom = 4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = DhunloBranding.PRODUCT_NAME,
                            style =
                                DhunloGlassTokens.LabelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = if (isDark) Color(0xFF4CC9F0) else Color(0xFF0052D4),
                                ),
                        )
                        Text(
                            text = "•",
                            style = DhunloGlassTokens.LabelSmall.copy(color = DhunloGlassTokens.textTertiary(isDark)),
                        )
                        Text(
                            text = DhunloBranding.TAGLINE,
                            style =
                                DhunloGlassTokens.LabelSmall.copy(
                                    color = DhunloGlassTokens.textTertiary(isDark),
                                    fontWeight = FontWeight.Medium,
                                ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Text(
                        text = greeting,
                        style =
                            if (isWide) {
                                DhunloGlassTokens.DisplayLarge.copy(fontSize = 30.sp, lineHeight = 36.sp)
                            } else {
                                DhunloGlassTokens.HeadlineMedium.copy(fontSize = 22.sp, lineHeight = 28.sp)
                            },
                        fontWeight = FontWeight.Bold,
                        color = DhunloGlassTokens.textPrimary(isDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "Discover today's soundtrack",
                        style =
                            DhunloGlassTokens.BodyMedium.copy(
                                fontSize = if (isWide) 14.sp else 13.sp,
                                color = DhunloGlassTokens.textSecondary(isDark),
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(16.dp))

                DhunloBrandMark(size = if (isWide) 42.dp else 34.dp)
            }
        }
    }
}
