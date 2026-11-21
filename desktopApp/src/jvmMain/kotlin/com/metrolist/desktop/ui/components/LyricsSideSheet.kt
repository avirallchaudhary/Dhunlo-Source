@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.desktop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.app.UiState
import com.metrolist.desktop.state.playback.LyricsState
import com.metrolist.desktop.state.playback.PlayerState
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.close
import com.metrolist.shared.generated.resources.lyrics
import com.metrolist.shared.generated.resources.lyrics_menu
import com.metrolist.shared.model.LyricsProvider
import com.metrolist.shared.settings.PlayerBackgroundStyle
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.LiquidBackground
import com.metrolist.shared.ui.design.glass.DhunloGlassTokens
import com.metrolist.shared.ui.design.glass.glassBorder
import com.metrolist.shared.ui.components.images.AsyncImage
import com.metrolist.shared.ui.components.lyrics.LyricsContrastScrim
import com.metrolist.shared.ui.components.lyrics.LyricsOffsetEditor
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.ui.navigation.navigationPopupSource
import com.metrolist.shared.ui.player.PlayerGradientBackground
import com.metrolist.shared.ui.theme.getGradientColors
import com.metrolist.shared.ui.utils.ArtworkBlurStyle
import org.jetbrains.compose.resources.stringResource

@Composable
fun LyricsDrawer(
    drawerOpen: Boolean,
    animatedDrawerWidth: Dp,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = { UiState.showLyricsDrawer = false },
    isVisible: Boolean = true,
) {
    AnimatedVisibility(
        visible = drawerOpen,
        modifier = modifier.fillMaxHeight(),
        enter =
            expandHorizontally(
                expandFrom = Alignment.End,
                animationSpec = tween(durationMillis = 280, easing = EaseOutCubic),
            ) + fadeIn(animationSpec = tween(200)),
        exit =
            shrinkHorizontally(
                shrinkTowards = Alignment.End,
                animationSpec = tween(durationMillis = 250, easing = EaseOutCubic),
            ) + fadeOut(animationSpec = tween(150)),
    ) {
        DesktopLyricsContent(
            onClose = onClose,
            colorScheme = colorScheme,
            modifier = Modifier.width(animatedDrawerWidth),
            isVisible = isVisible,
        )
    }
}

@Composable
internal fun DesktopLyricsContent(
    onClose: () -> Unit = { UiState.showLyricsDrawer = false },
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
) {
    val activeSeedColor = UiState.seedColor
    val isDark = Settings.shared.getCurrentThemeIsDark()
    val isImmersiveBg = Settings.shared.playerBackgroundStyle != PlayerBackgroundStyle.SOLID

    val textBackgroundColor by animateColorAsState(
        targetValue =
            if (!isDark) {
                DhunloGlassTokens.WaterPearlText
            } else {
                when (Settings.shared.playerBackgroundStyle) {
                    PlayerBackgroundStyle.BLURRED_ARTWORK,
                    PlayerBackgroundStyle.GRADIENT,
                    PlayerBackgroundStyle.ANIMATED_GRADIENT,
                    PlayerBackgroundStyle.LIQUID,
                    -> Color.White

                    PlayerBackgroundStyle.SOLID -> colorScheme.onSurface
                }
            },
        label = "TextBackgroundColor",
    )

    val headerBg =
        if (!isDark) {
            DhunloGlassTokens.WaterFrostedPearlFill
        } else if (isImmersiveBg) {
            Color.White.copy(alpha = 0.14f)
        } else {
            colorScheme.surfaceContainerHigh
        }
    val headerTextColor = if (!isDark) DhunloGlassTokens.WaterPearlText else if (isImmersiveBg) Color.White else colorScheme.onSurface
    val headerIconTint = if (!isDark) DhunloGlassTokens.WaterPearlText.copy(alpha = 0.85f) else if (isImmersiveBg) Color.White.copy(alpha = 0.85f) else colorScheme.onSurfaceVariant
    val panelShape = RoundedCornerShape(16.dp)

    Surface(
        modifier =
            modifier
                .fillMaxHeight()
                .glassBorder(
                    shape = panelShape,
                    borderWidth = DhunloGlassTokens.BorderHairline,
                    intensity = if (isDark) 0.8f else 0.65f,
                ),
        shape = panelShape,
        color =
            if (!isDark) {
                DhunloGlassTokens.WaterFrostedPearlDeepFill
            } else if (isImmersiveBg) {
                Color.Black
            } else {
                colorScheme.surface
            },
        tonalElevation = 1.dp,
    ) {
        val drawerContent = @Composable {
            if (isImmersiveBg && isDark) {
                val style = Settings.shared.playerBackgroundStyle
                val hasArtwork = style == PlayerBackgroundStyle.BLURRED_ARTWORK || style == PlayerBackgroundStyle.LIQUID
                LyricsContrastScrim(
                    backgroundColors =
                        when {
                            style == PlayerBackgroundStyle.BLURRED_ARTWORK -> listOf(colorScheme.surfaceContainerHigh)
                            style == PlayerBackgroundStyle.LIQUID -> listOf(Color.Black)
                            else -> getGradientColors(activeSeedColor)
                        },
                    artworkUrl = PlayerState.currentArtworkUrl.takeIf { hasArtwork },
                    artworkScrimAlpha = 0.45f,
                    isLiquid = style == PlayerBackgroundStyle.LIQUID,
                )
            }
            Column(modifier = Modifier.fillMaxSize()) {
                var showLyricsMenu by remember { mutableStateOf(false) }
                Surface(
                    color = headerBg,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = if (isImmersiveBg) 0.dp else 2.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(Res.string.lyrics),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = headerTextColor,
                            modifier = Modifier.weight(1f),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box {
                                IconButton(
                                    onClick = { showLyricsMenu = !showLyricsMenu },
                                    modifier = Modifier.size(36.dp).navigationPopupSource(),
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        MetrolistIcons.Outlined.MoreVert,
                                        contentDescription = stringResource(Res.string.lyrics_menu),
                                        modifier = Modifier.size(20.dp),
                                        tint = headerIconTint,
                                    )
                                }
                                LyricsActionsMenuCard(
                                    expanded = showLyricsMenu,
                                    colorScheme = colorScheme,
                                    onDismiss = { showLyricsMenu = false },
                                    onShowLyricsOffset = {
                                        showLyricsMenu = false
                                        LyricsState.showLyricsOffsetEditor = true
                                    },
                                )
                            }
                            IconButton(
                                onClick = {
                                    showLyricsMenu = false
                                    LyricsState.showLyricsOffsetEditor = false
                                    onClose()
                                },
                                modifier = Modifier.size(36.dp),
                                shapes = IconButtonDefaults.shapes(),
                            ) {
                                Icon(
                                    MetrolistIcons.Default.Close,
                                    contentDescription = stringResource(Res.string.close),
                                    modifier = Modifier.size(20.dp),
                                    tint = headerIconTint,
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    val currentSongId = PlayerState.currentTrack?.id
                    LaunchedEffect(
                        currentSongId,
                        Settings.shared.lyricsProviderOrder,
                        Settings.shared.lyricsEnabledProviders,
                    ) {
                        if (currentSongId != null) {
                            PlayerState.currentTrack?.let { track ->
                                val currentProvider =
                                    LyricsState.currentLyricsProvider?.let {
                                        LyricsProvider.parseTokenFromSettings(it)
                                    }
                                val isCurrentProviderDisabled =
                                    currentProvider in LyricsProvider.userConfigurable &&
                                        currentProvider !in LyricsState.lyricsEnabledProviders
                                LyricsState.fetchLyrics(track, skipCache = isCurrentProviderDisabled)
                            }
                        }
                    }
                    DesktopLyricsContent(textColor = textBackgroundColor, isVisible = isVisible)
                    if (showLyricsMenu) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { showLyricsMenu = false },
                        )
                    }
                }

                AnimatedVisibility(
                    visible = LyricsState.showLyricsOffsetEditor,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    LyricsOffsetEditor(
                        offsetMs = LyricsState.currentLyricsOffsetMs,
                        onOffsetChange = { offset ->
                            LyricsState.updateLyricsOffset(offset)
                        },
                        onDone = { LyricsState.showLyricsOffsetEditor = false },
                        contentColor = textBackgroundColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (Settings.shared.playerBackgroundStyle) {
                PlayerBackgroundStyle.GRADIENT -> {
                    if (isDark) {
                        PlayerGradientBackground(getGradientColors(activeSeedColor), animate = false) {
                            drawerContent()
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(DhunloGlassTokens.FrostedPearlWaterGradient)) {
                            drawerContent()
                        }
                    }
                }

                PlayerBackgroundStyle.ANIMATED_GRADIENT -> {
                    if (isDark) {
                        PlayerGradientBackground(
                            getGradientColors(activeSeedColor),
                            animate = true,
                            active = PlayerState.isPlaying,
                        ) {
                            drawerContent()
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(DhunloGlassTokens.FrostedPearlWaterGradient)) {
                            drawerContent()
                        }
                    }
                }

                PlayerBackgroundStyle.BLURRED_ARTWORK -> {
                    val bgBoxColor = if (isDark) Color.Black else DhunloGlassTokens.WaterFrostedPearlDeepFill
                    val scrimCol = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.55f)
                    Box(modifier = Modifier.fillMaxSize().background(bgBoxColor))
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            url = PlayerState.currentArtworkUrl ?: "",
                            artworkPreview = PlayerState.artworkPreview,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            blur =
                                ArtworkBlurStyle(
                                    radius = 60.dp,
                                    backgroundColor = bgBoxColor,
                                    scale = 1.6f,
                                    scrimColor = scrimCol,
                                ),
                        )
                    }
                    drawerContent()
                }

                PlayerBackgroundStyle.LIQUID -> {
                    if (isDark) {
                        LiquidBackground(
                            artworkUrl = PlayerState.currentArtworkUrl,
                            isPlaying = PlayerState.isPlaying,
                            visible = true,
                        )
                    } else {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(DhunloGlassTokens.FrostedPearlWaterGradient),
                        )
                    }
                    drawerContent()
                }

                PlayerBackgroundStyle.SOLID -> {
                    val solidBg = if (isDark) colorScheme.surfaceContainer else DhunloGlassTokens.WaterFrostedPearlDeepFill
                    Box(modifier = Modifier.fillMaxSize().background(solidBg)) {
                        drawerContent()
                    }
                }
            }
        }
    }
}
