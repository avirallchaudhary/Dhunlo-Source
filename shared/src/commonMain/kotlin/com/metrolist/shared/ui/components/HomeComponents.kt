@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.see_all
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.images.AsyncImage
import com.metrolist.shared.ui.design.glass.DhunloGlassTokens
import com.metrolist.shared.ui.design.glass.glassBorder
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.ui.utils.artistArtworkShape
import org.jetbrains.compose.resources.stringResource

@Composable
fun GenreCard(
    title: String,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val isDark = Settings.shared.getCurrentThemeIsDark()
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .height(52.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .shadow(
                    elevation = 4.dp,
                    shape = shape,
                    ambientColor = DhunloGlassTokens.ShadowAmbient,
                    spotColor = DhunloGlassTokens.ShadowAmbient,
                )
                .glassBorder(
                    shape = shape,
                    borderWidth = DhunloGlassTokens.BorderHairline,
                    intensity = if (isDark) 0.75f else 0.55f,
                ),
        shape = shape,
        color = DhunloGlassTokens.floatingGlassFill(isDark),
        contentColor = DhunloGlassTokens.textPrimary(isDark),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 14.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = DhunloGlassTokens.textPrimary(isDark),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    thumbnailUrl: String? = null,
    isArtistEndpoint: Boolean = false,
    artworkSeed: String = thumbnailUrl.orEmpty(),
    seeAllLabel: String? = null,
    showSeeAllLabel: Boolean = true,
    shuffleLabel: String? = null,
    playAllLabel: String? = null,
    onClick: (() -> Unit)? = null,
    onPlayAllClick: (() -> Unit)? = null,
    onShuffleClick: (() -> Unit)? = null,
    isWide: Boolean = false,
) {
    val isDark = Settings.shared.getCurrentThemeIsDark()
    val titleStyle = if (isWide) MaterialTheme.typography.headlineMediumEmphasized else MaterialTheme.typography.headlineSmallEmphasized
    val rowHeight = if (isWide || label != null) 60.dp else 50.dp

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = rowHeight)
                .then(
                    if (onClick != null) {
                        Modifier.clip(RoundedCornerShape(12.dp)).pointerHoverIcon(PointerIcon.Hand).clickable(
                            role = Role.Button,
                            onClickLabel = seeAllLabel ?: stringResource(Res.string.see_all),
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (thumbnailUrl != null) {
            val thumbShape = if (isArtistEndpoint) artistArtworkShape(artworkSeed) else RoundedCornerShape(8.dp)
            AsyncImage(
                url = thumbnailUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(48.dp)
                        .shadow(4.dp, thumbShape, ambientColor = DhunloGlassTokens.ShadowAmbient)
                        .clip(thumbShape)
                        .glassBorder(thumbShape, borderWidth = DhunloGlassTokens.BorderHairline, intensity = 0.7f),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = DhunloGlassTokens.textSecondary(isDark),
                )
            }

            Text(
                text = title,
                style = titleStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = DhunloGlassTokens.textPrimary(isDark),
            )
        }

        if (onClick != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showSeeAllLabel && seeAllLabel != null) {
                    Text(
                        text = seeAllLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF4CC9F0) else Color(0xFF0052D4),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                SectionActionButton(
                    icon = MetrolistIcons.Outlined.ChevronRight,
                    contentDescription = null,
                    isWide = isWide,
                )
            }
        } else if (onShuffleClick != null) {
            SectionActionButton(
                icon = MetrolistIcons.Rounded.Shuffle,
                contentDescription = shuffleLabel,
                onClick = onShuffleClick,
                isWide = isWide,
            )
        } else if (onPlayAllClick != null) {
            SectionActionButton(
                icon = MetrolistIcons.Default.PlayArrow,
                contentDescription = playAllLabel,
                onClick = onPlayAllClick,
                isWide = isWide,
            )
        }
    }
}

@Composable
private fun SectionActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: (() -> Unit)? = null,
    isWide: Boolean = false,
) {
    val boxSize = if (isWide) 56.dp else 48.dp
    val surfaceSize = Modifier.size(40.dp, 32.dp)
    val iconSize = 20.dp
    val pillShape = RoundedCornerShape(100.dp)
    val actionModifier =
        surfaceSize
            .shadow(
                elevation = 4.dp,
                shape = pillShape,
                ambientColor = DhunloGlassTokens.ShadowAmbient,
                spotColor = DhunloGlassTokens.ShadowAmbient,
            )
            .glassBorder(
                shape = pillShape,
                borderWidth = DhunloGlassTokens.BorderHairline,
                intensity = 0.85f,
            )
            .pointerHoverIcon(PointerIcon.Hand)

    Box(
        modifier = Modifier.size(boxSize),
        contentAlignment = Alignment.Center,
    ) {
        val content: @Composable () -> Unit = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = DhunloGlassTokens.TextPrimary,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
        if (onClick != null) {
            Surface(
                modifier = actionModifier,
                shape = pillShape,
                color = DhunloGlassTokens.ControlGlassFill,
                contentColor = DhunloGlassTokens.TextPrimary,
                onClick = onClick,
                content = content,
            )
        } else {
            Surface(
                modifier = actionModifier,
                shape = pillShape,
                color = DhunloGlassTokens.ControlGlassFill,
                contentColor = DhunloGlassTokens.TextPrimary,
                content = content,
            )
        }
    }
}
