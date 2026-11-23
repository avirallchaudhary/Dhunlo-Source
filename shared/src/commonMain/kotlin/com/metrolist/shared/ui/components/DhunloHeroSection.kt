package com.metrolist.shared.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.model.EpisodeItem
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.PodcastItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.YTItem
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.settings.displayName
import com.metrolist.shared.ui.components.images.AsyncImage
import com.metrolist.shared.ui.design.glass.DhunloGlassTokens
import com.metrolist.shared.ui.design.glass.LiquidWaterVariant
import com.metrolist.shared.ui.design.glass.glassBorder
import com.metrolist.shared.ui.design.glass.liquidWaterMaterial
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.viewmodels.HomeViewModel.HomeUiState

/**
 * Selects a featured item for the Hero Spotlight using REAL music data from [uiState].
 * Strictly evaluates actual loaded content: Quick Picks, Trending, New Releases,
 * Album Charts, Keep Listening, or home sections.
 * Returns null if no content is loaded; never fabricates fake items.
 */
fun selectFeaturedHomeItem(uiState: HomeUiState): YTItem? =
    uiState.quickPicks.firstOrNull()
        ?: uiState.trending.firstOrNull()
        ?: uiState.newReleases.firstOrNull()
        ?: uiState.albumCharts.firstOrNull()
        ?: uiState.keepListening.firstOrNull()
        ?: uiState.sections.firstOrNull { it.items.isNotEmpty() }?.items?.firstOrNull()
        ?: uiState.data.orEmpty().firstOrNull { it.items.isNotEmpty() }?.items?.firstOrNull()
        ?: uiState.speedDialItems.firstOrNull()

/**
 * Returns a human-friendly category label for the Hero Spotlight badge.
 */
fun heroItemBadgeLabel(item: YTItem): String =
    when (item) {
        is SongItem -> "SPOTLIGHT • FEATURED TRACK"
        is AlbumItem -> "SPOTLIGHT • FEATURED ALBUM"
        is PlaylistItem -> "SPOTLIGHT • FEATURED PLAYLIST"
        is PodcastItem, is EpisodeItem -> "SPOTLIGHT • PODCAST"
        is ArtistItem -> "SPOTLIGHT • ARTIST"
        else -> "SPOTLIGHT • FOR YOU"
    }

/**
 * Returns artist or author subtitle for the Hero Spotlight.
 */
fun heroItemSubtitle(item: YTItem): String? =
    when (item) {
        is SongItem -> item.artists.joinToString(", ") { it.name }.takeIf(String::isNotBlank)
        is AlbumItem -> item.artists.joinToString(", ") { it.name }.takeIf(String::isNotBlank) ?: item.year
        is PlaylistItem -> item.author
        is PodcastItem -> item.author
        is EpisodeItem -> item.author?.displayName ?: item.author?.name
        else -> null
    }

/**
 * Cinematic Liquid Glass Featured Music Hero Card.
 *
 * Renders an Apple Music-inspired spotlight banner with:
 * - Real music artwork with continuous squircle corners & ambient glow
 * - Category badge with luminous cyan accent
 * - Prominent display typography for song/album title
 * - Artist and album metadata
 * - Translucent Liquid Glass Play/Pause control with live state
 * - Adaptive layout: compact cinematic card on mobile, wide panoramic hero on desktop
 */
@Composable
fun DhunloHeroSection(
    item: YTItem,
    currentSongId: State<String?>,
    isPlaying: State<Boolean>,
    onItemClick: (YTItem) -> Unit,
    modifier: Modifier = Modifier,
    onPlayAlbum: ((AlbumItem) -> Unit)? = null,
    onPlaySongs: ((List<SongItem>) -> Unit)? = null,
    onArtistClick: ((String) -> Unit)? = null,
    isWide: Boolean = false,
) {
    val isCurrent = currentSongId.value == item.id
    val itemIsPlaying = isCurrent && isPlaying.value

    val onPlayAction: () -> Unit = {
        when {
            item is SongItem && onPlaySongs != null -> onPlaySongs(listOf(item))
            item is AlbumItem && onPlayAlbum != null -> onPlayAlbum(item)
            else -> onItemClick(item)
        }
    }

    val horizontalPadding = if (isWide) 24.dp else 16.dp
    val cardShape = if (isWide) RoundedCornerShape(26.dp) else RoundedCornerShape(22.dp)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = 1400.dp)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 6.dp),
        ) {
            AnimatedContent(
                targetState = item,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "dhunlo_hero_transition",
            ) { targetItem ->
                HeroCardContent(
                    item = targetItem,
                    isPlaying = itemIsPlaying,
                    cardShape = cardShape,
                    isWide = isWide,
                    onCardClick = { onItemClick(targetItem) },
                    onPlayClick = onPlayAction,
                    onArtistClick = onArtistClick,
                )
            }
        }
    }
}

@Composable
private fun HeroCardContent(
    item: YTItem,
    isPlaying: Boolean,
    cardShape: RoundedCornerShape,
    isWide: Boolean,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit,
    onArtistClick: ((String) -> Unit)?,
) {
    val isDark = Settings.shared.getCurrentThemeIsDark()
    val badge = remember(item) { heroItemBadgeLabel(item) }
    val subtitle = remember(item) { heroItemSubtitle(item) }
    val artworkUrl = item.thumbnail
    val artworkShape = if (isWide) RoundedCornerShape(20.dp) else RoundedCornerShape(16.dp)
    val artworkSize = if (isWide) 160.dp else 110.dp

    val customBorderBrush =
        remember(isDark) {
            if (isDark) {
                DhunloGlassTokens.brandSpecularBorderBrush(intensity = 0.85f)
            } else {
                DhunloGlassTokens.specularBorderBrush(intensity = 0.95f)
            }
        }

    val ambientBackdropBrush =
        remember(isDark) {
            if (isDark) {
                Brush.horizontalGradient(
                    0.00f to Color(0xFF8A56FF).copy(alpha = 0.16f),
                    0.45f to Color(0xFF4361EE).copy(alpha = 0.08f),
                    1.00f to Color.Transparent,
                )
            } else {
                Brush.horizontalGradient(
                    0.00f to Color(0xFF38BDF8).copy(alpha = 0.18f),
                    0.45f to Color(0xFF2563EB).copy(alpha = 0.08f),
                    1.00f to Color.Transparent,
                )
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = if (isWide) 210.dp else 160.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = cardShape,
                    ambientColor = if (isDark) DhunloGlassTokens.ShadowAmbient else Color(0x18000000),
                    spotColor = if (isDark) DhunloGlassTokens.ShadowAmbient else Color(0x18000000),
                )
                .clip(cardShape)
                .background(if (isDark) DhunloGlassTokens.FloatingGlassDarkFill else DhunloGlassTokens.WaterFrostedPearlDeepFill)
                .glassBorder(
                    shape = cardShape,
                    borderWidth = DhunloGlassTokens.BorderHairline,
                    customBrush = customBorderBrush,
                )
                .clickable(
                    role = Role.Button,
                    onClick = onCardClick,
                ).pointerHoverIcon(PointerIcon.Hand),
    ) {
        // Atmospheric ambient backdrop gradient
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(ambientBackdropBrush),
        )

        // Subtle bottom-to-top contrast gradient
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        if (isDark) {
                            Brush.verticalGradient(
                                0.00f to Color.Transparent,
                                0.70f to Color(0x6607080B),
                                1.00f to Color(0xCC07080B),
                            )
                        } else {
                            Brush.verticalGradient(
                                0.00f to Color.Transparent,
                                0.70f to Color(0x30E2E8F0),
                                1.00f to Color(0x60CBD5E1),
                            )
                        },
                    ),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(if (isWide) 22.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // High-resolution Artwork
            Box(
                modifier =
                    Modifier
                        .size(artworkSize)
                        .shadow(
                            elevation = 8.dp,
                            shape = artworkShape,
                            ambientColor = if (isDark) DhunloGlassTokens.ShadowAmbient else Color(0x14000000),
                            spotColor = if (isDark) DhunloGlassTokens.ShadowAmbient else Color(0x14000000),
                        )
                        .clip(artworkShape)
                        .background(if (isDark) DhunloGlassTokens.ObsidianElevated else Color(0xFFE2E8F0))
                        .glassBorder(
                            shape = artworkShape,
                            borderWidth = DhunloGlassTokens.BorderHairline,
                            intensity = 0.75f,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                if (artworkUrl != null) {
                    AsyncImage(
                        url = artworkUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = MetrolistIcons.Default.MusicNote,
                        contentDescription = null,
                        tint = DhunloGlassTokens.textTertiary(isDark),
                        modifier = Modifier.size(artworkSize * 0.45f),
                    )
                }
            }

            Spacer(Modifier.width(if (isWide) 22.dp else 14.dp))

            // Metadata and Controls Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val pillAccent = if (isDark) Color(0xFF4CC9F0) else Color(0xFF0052D4)

                // Category Tag Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF4CC9F0).copy(alpha = 0.14f) else Color(0xFF0052D4).copy(alpha = 0.10f))
                            .glassBorder(
                                shape = CircleShape,
                                borderWidth = DhunloGlassTokens.BorderHairline,
                                intensity = 0.6f,
                            )
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                ) {
                    Icon(
                        imageVector = MetrolistIcons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = pillAccent,
                        modifier = Modifier.size(11.dp),
                    )
                    Text(
                        text = badge,
                        style =
                            DhunloGlassTokens.LabelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = pillAccent,
                            ),
                        maxLines = 1,
                    )
                }

                // Item Title
                Text(
                    text = item.title,
                    style =
                        if (isWide) {
                            DhunloGlassTokens.HeadlineMedium.copy(
                                fontSize = 24.sp,
                                lineHeight = 30.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            DhunloGlassTokens.TitleMedium.copy(
                                fontSize = 17.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                    color = DhunloGlassTokens.textPrimary(isDark),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Subtitle / Artist
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style =
                            DhunloGlassTokens.BodyMedium.copy(
                                fontSize = if (isWide) 14.sp else 13.sp,
                                color = DhunloGlassTokens.textSecondary(isDark),
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Extra details (album name if SongItem)
                if (item is SongItem && item.album != null && item.album.name.isNotBlank()) {
                    Text(
                        text = item.album.name,
                        style =
                            DhunloGlassTokens.LabelSmall.copy(
                                fontSize = 11.sp,
                                color = DhunloGlassTokens.textTertiary(isDark),
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(if (isWide) 8.dp else 4.dp))

                // Action Controls Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Primary Liquid Glass Play/Pause Button
                    val playInteractionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier =
                            Modifier
                                .size(if (isWide) 46.dp else 40.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = if (isDark) DhunloGlassTokens.ShadowAmbient else Color(0x14000000),
                                    spotColor = if (isDark) DhunloGlassTokens.ShadowAmbient else Color(0x14000000),
                                )
                                .clip(CircleShape)
                                .liquidWaterMaterial(
                                    variant = LiquidWaterVariant.PRIMARY_PLAY,
                                    shape = CircleShape,
                                    interactionSource = playInteractionSource,
                                    showBubbles = true,
                                    specularIntensity = 1.35f,
                                    outerGlow = true,
                                )
                                .clickable(
                                    role = Role.Button,
                                    onClick = onPlayClick,
                                    interactionSource = playInteractionSource,
                                    indication = ripple(bounded = true, color = Color.White),
                                ).pointerHoverIcon(PointerIcon.Hand),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector =
                                if (isPlaying) {
                                    MetrolistIcons.Default.Pause
                                } else {
                                    MetrolistIcons.Default.PlayArrow
                                },
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isDark) Color.White else DhunloGlassTokens.WaterPearlText,
                            modifier = Modifier.size(if (isWide) 24.dp else 22.dp),
                        )
                    }

                    if (isWide) {
                        Text(
                            text = if (isPlaying) "Playing now" else "Play spotlight",
                            style =
                                DhunloGlassTokens.BodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = DhunloGlassTokens.textPrimary(isDark),
                                ),
                        )
                    }
                }
            }
        }
    }
}
