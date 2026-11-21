@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.shared.ui.components.focusOutline
import com.metrolist.shared.ui.design.glass.DhunloGlassTokens
import com.metrolist.shared.ui.design.glass.GlassTier
import com.metrolist.shared.ui.design.glass.glassBorder
import com.metrolist.shared.ui.design.glass.glassPressFeedback
import com.metrolist.shared.ui.design.glass.liquidGlassSurface

val LocalFloatingNavBar = compositionLocalOf { false }

private val FloatingBarHeight = 64.dp
private val FloatingItemHeight = 48.dp

@Composable
internal fun <T> FloatingNavigation(
    items: List<AdaptiveNavigationItem<T>>,
    selectedKey: T?,
    onSelect: (T) -> Unit,
    navigationVisible: Boolean,
    separateKey: T?,
    itemModifier: @Composable (T) -> Modifier,
    onBarHeightChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = navigationVisible,
        modifier = modifier,
        enter =
            expandVertically(
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
                expandFrom = Alignment.Bottom,
            ),
        exit =
            shrinkVertically(
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
                shrinkTowards = Alignment.Bottom,
            ),
    ) {
        FloatingNavigationBar(
            items = items,
            selectedKey = selectedKey,
            onSelect = onSelect,
            separateKey = separateKey,
            itemModifier = itemModifier,
            modifier =
                Modifier.onSizeChanged { size ->
                    onBarHeightChanged(with(density) { size.height.toDp() })
                },
        )
    }
}

@Composable
private fun <T> FloatingNavigationBar(
    items: List<AdaptiveNavigationItem<T>>,
    selectedKey: T?,
    onSelect: (T) -> Unit,
    separateKey: T?,
    itemModifier: @Composable (T) -> Modifier,
    modifier: Modifier = Modifier,
) {
    val pillItems = remember(items, separateKey) { items.filter { it.key != separateKey } }
    val separateItem = remember(items, separateKey) { items.firstOrNull { it.key == separateKey } }

    Row(
        modifier =
            modifier
                .wrapContentWidth()
                .windowInsetsPadding(ShortNavigationBarDefaults.windowInsets)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Floating Liquid Glass Navigation Dock
        Box(
            modifier =
                Modifier
                    .wrapContentWidth()
                    .height(FloatingBarHeight)
                    .liquidGlassSurface(
                        tier = GlassTier.FLOATING_DARK,
                        shape = CircleShape,
                        borderWidth = DhunloGlassTokens.BorderStandard,
                        specularIntensity = 1.0f,
                        elevation = DhunloGlassTokens.ElevationFloating,
                    )
                    .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pillItems.forEach { item ->
                    FloatingNavigationItem(
                        item = item,
                        selected = item.key == selectedKey,
                        onClick = { onSelect(item.key) },
                        modifier = itemModifier(item.key),
                    )
                }
            }
        }

        // Separate Floating Glass Action Button (e.g. Search)
        if (separateItem != null) {
            val selected = separateItem.key == selectedKey
            val interactionSource = remember { MutableInteractionSource() }
            val iconTint by animateColorAsState(
                targetValue = if (selected) Color.White else DhunloGlassTokens.TextSecondary,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f),
                label = "floatingSearchIconTint",
            )
            Box(
                modifier =
                    Modifier
                        .size(FloatingBarHeight)
                        .liquidGlassSurface(
                            tier = if (selected) GlassTier.CONTROL else GlassTier.FLOATING_DARK,
                            shape = CircleShape,
                            borderWidth = DhunloGlassTokens.BorderStandard,
                            specularIntensity = if (selected) 1.4f else 1.0f,
                            elevation = DhunloGlassTokens.ElevationFloating,
                        )
                        .glassPressFeedback(interactionSource)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(),
                            onClick = { onSelect(separateItem.key) },
                        )
                        .then(itemModifier(separateItem.key)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) separateItem.selectedIcon else separateItem.icon,
                    contentDescription = separateItem.label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun <T> FloatingNavigationItem(
    item: AdaptiveNavigationItem<T>,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val accentColor = MaterialTheme.colorScheme.primary

    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.0f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "floatingItemIndicatorAlpha",
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) DhunloGlassTokens.TextPrimary else DhunloGlassTokens.TextSecondary,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f),
        label = "floatingItemContent",
    )

    val activeBackgroundModifier =
        if (indicatorAlpha > 0.01f) {
            Modifier
                .clip(CircleShape)
                .background(
                    color = DhunloGlassTokens.ControlGlassFill.copy(
                        alpha = DhunloGlassTokens.ControlGlassFill.alpha * indicatorAlpha,
                    ),
                    shape = CircleShape,
                )
                .glassBorder(
                    shape = CircleShape,
                    borderWidth = DhunloGlassTokens.BorderHairline,
                    intensity = indicatorAlpha,
                    customBrush = DhunloGlassTokens.activeSpecularBorderBrush(accentColor, intensity = indicatorAlpha),
                )
        } else {
            Modifier
        }

    Row(
        modifier =
            modifier
                .height(FloatingItemHeight)
                .glassPressFeedback(interactionSource)
                .then(activeBackgroundModifier)
                .focusOutline(CircleShape)
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.Tab,
                    interactionSource = interactionSource,
                    indication = ripple(),
                )
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter =
                fadeIn() +
                    expandHorizontally(
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ),
            exit =
                fadeOut() +
                    shrinkHorizontally(
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = item.selectedIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
        }
        Text(
            text = item.label,
            color = contentColor,
            style = DhunloGlassTokens.TitleMedium.copy(fontSize = 14.sp),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
