package com.metrolist.shared.ui.design.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.theme.rememberReducedMotion
import com.metrolist.shared.ui.utils.nativeBlurCompat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Surface tier determining optical depth, blur radius, fill translucency, and highlight intensity.
 */
enum class GlassTier {
    /** Tier 1: Panels, cards, side sheets, and lists (~6% white + 28dp blur). */
    PANEL,

    /** Tier 2: Translucent floating menus and light dialogs (~10% white + 44dp blur). */
    FLOATING_LIGHT,

    /** Tier 2: Floating dark refractive islands (Floating MiniPlayer, NavSuite dock) (~75% dark obsidian + 44dp blur). */
    FLOATING_DARK,

    /** Tier 3: Media controls, interactive pills, secondary buttons (~18% white + 16dp blur). */
    CONTROL,

    /** Tier 3: Primary active play pill (100% white prominent pill). */
    ACTIVE_PILL,
}

/**
 * Applies the authentic Liquid Glass surface style to any Compose component.
 *
 * Automatically inspects the platform's hardware blur support ([getBlurSupportTier]):
 * - If hardware blur is supported (Android 31+, Windows Desktop GPU): applies translucent fill,
 *   [nativeBlurCompat], directional specular border, and subtle inner glare.
 * - If hardware blur is unsupported (Android < 31, Desktop software rendering): gracefully
 *   falls back to high-opacity frosted acrylic tints without expensive per-frame CPU re-filtering,
 *   preserving rock-solid 60 FPS scrolling.
 */
@Composable
fun Modifier.liquidGlassSurface(
    tier: GlassTier = GlassTier.PANEL,
    shape: Shape = DhunloGlassTokens.ShapeCard,
    borderWidth: Dp = DhunloGlassTokens.BorderStandard,
    specularIntensity: Float = 1.0f,
    blurRadius: Dp? = null,
    customFill: Color? = null,
    elevation: Dp = DhunloGlassTokens.ElevationNone,
    innerReflection: Boolean = true,
    supportTier: BlurSupportTier = getBlurSupportTier(),
): Modifier {
    val targetBlur =
        blurRadius ?: when (tier) {
            GlassTier.PANEL -> DhunloGlassTokens.BlurPanel
            GlassTier.FLOATING_LIGHT, GlassTier.FLOATING_DARK -> DhunloGlassTokens.BlurIsland
            GlassTier.CONTROL -> DhunloGlassTokens.BlurSubtle
            GlassTier.ACTIVE_PILL -> 0.dp
        }

    val isFallback = supportTier == BlurSupportTier.NONE

    val fillColor =
        customFill ?: when {
            tier == GlassTier.ACTIVE_PILL -> DhunloGlassTokens.ActivePillFill
            isFallback && tier == GlassTier.PANEL -> DhunloGlassTokens.AcrylicPanelFallbackFill
            isFallback -> DhunloGlassTokens.AcrylicFallbackFill
            tier == GlassTier.PANEL -> DhunloGlassTokens.PanelGlassFill
            tier == GlassTier.FLOATING_LIGHT -> DhunloGlassTokens.FloatingGlassFill
            tier == GlassTier.FLOATING_DARK -> DhunloGlassTokens.FloatingGlassDarkFill
            tier == GlassTier.CONTROL -> DhunloGlassTokens.ControlGlassFill
            else -> DhunloGlassTokens.PanelGlassFill
        }

    val shadowModifier =
        if (elevation > 0.dp) {
            Modifier.shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = DhunloGlassTokens.ShadowAmbient,
                spotColor = DhunloGlassTokens.ShadowAmbient,
            )
        } else {
            Modifier
        }

    val blurModifier =
        if (!isFallback && targetBlur > 0.dp) {
            Modifier.nativeBlurCompat(radiusPx = targetBlur.value * 2.5f)
        } else {
            Modifier
        }

    return this
        .then(shadowModifier)
        .clip(shape)
        .background(color = fillColor, shape = shape)
        .glassBorder(
            shape = shape,
            borderWidth = borderWidth,
            intensity = specularIntensity,
        )
        .glassInnerReflection(
            shape = shape,
            enabled = innerReflection && !isFallback && tier != GlassTier.ACTIVE_PILL,
        )
}

/**
 * Draws a 135° angle directional specular reflection border simulating light refraction
 * through real glass without clipping child content.
 */
fun Modifier.glassBorder(
    shape: Shape,
    borderWidth: Dp = DhunloGlassTokens.BorderStandard,
    intensity: Float = 1.0f,
    customBrush: Brush? = null,
): Modifier =
    this.drawWithContent {
        drawContent()
        if (borderWidth > 0.dp && intensity > 0f) {
            val strokeWidthPx = borderWidth.toPx()
            val brush = customBrush ?: DhunloGlassTokens.specularBorderBrush(intensity = intensity)
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, this),
                brush = brush,
                style = Stroke(width = strokeWidthPx),
            )
        }
    }

private val GlareLineBrush =
    Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = 0.08f),
        0.5f to Color.White.copy(alpha = 0.02f),
        1.0f to Color.Transparent,
    )

/**
 * Draws a subtle inner glare line along the top rim of a glass surface, simulating thickness.
 */
fun Modifier.glassInnerReflection(
    shape: Shape,
    enabled: Boolean = true,
): Modifier =
    if (!enabled) {
        this
    } else {
        this.drawWithContent {
            drawContent()
            if (size.width > 0f && size.height > 0f) {
                // Subtle top-rim glare line
                val glareHeight = (size.height * 0.35f).coerceAtMost(32.dp.toPx())
                drawRect(
                    brush = GlareLineBrush,
                    topLeft = Offset.Zero,
                    size = Size(size.width, glareHeight),
                )
            }
        }
    }

/**
 * Applies iOS-style tactile spring scale feedback on press.
 * Automatically respects system reduced-motion preferences via [rememberReducedMotion].
 */
@Composable
fun Modifier.glassPressFeedback(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val reducedMotion = rememberReducedMotion()
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = if (isPressed && !reducedMotion) pressedScale else 1.0f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "glassPressFeedbackScale",
    )

    return this.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}

/**
 * Renders an accessible, high-contrast focus outline when navigated via keyboard or D-pad.
 */
fun Modifier.glassFocusOutline(
    shape: Shape = DhunloGlassTokens.ShapeCard,
    isFocused: Boolean,
    focusColor: Color = DhunloGlassTokens.FocusBorderColor,
    borderWidth: Dp = DhunloGlassTokens.BorderFocus,
    brush: Brush? = null,
): Modifier =
    if (!isFocused) {
        this
    } else {
        this.drawWithContent {
            drawContent()
            val strokeWidthPx = borderWidth.toPx()
            if (brush != null) {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    brush = brush,
                    style = Stroke(width = strokeWidthPx),
                )
            } else {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = focusColor,
                    style = Stroke(width = strokeWidthPx),
                )
            }
        }
    }

/**
 * Convenient Box container pre-configured with the Liquid Glass surface style.
 */
@Composable
fun DhunloGlassBox(
    modifier: Modifier = Modifier,
    tier: GlassTier = GlassTier.PANEL,
    shape: Shape = DhunloGlassTokens.ShapeCard,
    borderWidth: Dp = DhunloGlassTokens.BorderStandard,
    specularIntensity: Float = 1.0f,
    blurRadius: Dp? = null,
    customFill: Color? = null,
    elevation: Dp = DhunloGlassTokens.ElevationNone,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier.liquidGlassSurface(
                tier = tier,
                shape = shape,
                borderWidth = borderWidth,
                specularIntensity = specularIntensity,
                blurRadius = blurRadius,
                customFill = customFill,
                elevation = elevation,
            ),
        content = content,
    )
}

/**
 * Visual variant for the dynamic liquid-glass water material.
 */
enum class LiquidWaterVariant {
    /**
     * Translucent smoky obsidian liquid glass with cyan refraction sweep.
     * Used for dark theme playback controls and unselected chips.
     */
    SMOKY_OBSIDIAN,

    /**
     * Translucent frosted pearl liquid glass with ice-blue refraction.
     * Used for light theme playback controls and unselected chips.
     */
    FROSTED_PEARL,

    /**
     * Full-bleed electric-blue liquid-glass gradient with cyan highlights and micro-bubbles.
     * Fills the entire shape edge-to-edge across all corners. Used for selected chips and active states.
     */
    ELECTRIC_BLUE,

    /**
     * Luminous radial refraction liquid glass with deep clarity and prominent specular sheen.
     * Used for the primary Play/Pause button.
     */
    PRIMARY_PLAY,
}

/**
 * Deterministic micro-bubble layout parameter for dynamic liquid refraction.
 */
data class MicroBubbleDef(
    val relX: Float,
    val relY: Float,
    val radiusDp: Float,
    val phase: Float,
    val baseAlpha: Float = 0.55f,
)

val DefaultMicroBubbles: List<MicroBubbleDef> = listOf(
    MicroBubbleDef(0.20f, 0.34f, 2.4f, 0.0f, 0.65f),
    MicroBubbleDef(0.72f, 0.66f, 3.0f, 1.8f, 0.60f),
    MicroBubbleDef(0.48f, 0.28f, 1.8f, 3.2f, 0.50f),
    MicroBubbleDef(0.84f, 0.38f, 2.2f, 4.6f, 0.55f),
    MicroBubbleDef(0.14f, 0.68f, 2.0f, 2.4f, 0.45f),
)

/**
 * Applies the Stage 1 strong dynamic liquid-glass material to any Compose component.
 *
 * Implements:
 * - Translucent liquid glass surfaces visible against underlying backdrops
 * - Micro water bubbles with glassy specular glints and harmonic drift on idle
 * - Blue/cyan dynamic light refraction sweeps
 * - Glossy top curved meniscus reflection
 * - 135° directional specular rim border
 * - Press-triggered expanding water ripple and liquid spring compression (0.94f)
 * - Hover illumination boost
 * - Full-bleed saturation on active / selected state (edge-to-edge, no center-only highlight)
 * - Automatic dual theme adaptation: Liquid Obsidian (Dark) vs Frosted Pearl (Light)
 */
@Composable
fun Modifier.liquidWaterMaterial(
    variant: LiquidWaterVariant,
    shape: Shape = CircleShape,
    interactionSource: InteractionSource? = null,
    showBubbles: Boolean = true,
    specularIntensity: Float = 1.0f,
    outerGlow: Boolean = false,
    enabled: Boolean = true,
): Modifier {
    val isDark = Settings.shared.getCurrentThemeIsDark()
    val reducedMotion = rememberReducedMotion()

    val isPressed = if (interactionSource != null && enabled) {
        interactionSource.collectIsPressedAsState().value
    } else false

    val isHovered = if (interactionSource != null && enabled) {
        interactionSource.collectIsHoveredAsState().value
    } else false

    // Tactile spring scale compression on press (0.94f), slight expansion on hover (1.02f)
    val targetScale = when {
        isPressed && !reducedMotion -> 0.94f
        isHovered && !reducedMotion -> 1.02f
        else -> 1.0f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "liquidWaterScale",
    )

    // Gentle harmonic water drift cycle for micro-bubbles: animates smoothly on hover/press or primary play button
    val shouldAnimateDrift = !reducedMotion && (isHovered || isPressed || variant == LiquidWaterVariant.PRIMARY_PLAY)
    val infiniteTransition = rememberInfiniteTransition(label = "liquidDrift")
    val driftCycle by if (shouldAnimateDrift) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "driftCycle",
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Expanding water ripple on press
    val rippleProgress = remember { Animatable(1f) }
    LaunchedEffect(isPressed) {
        if (isPressed && !reducedMotion) {
            rippleProgress.snapTo(0f)
            rippleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            )
        }
    }

    // Hover illumination brightness
    val hoverAlpha by animateFloatAsState(
        targetValue = if (isHovered && enabled) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "hoverAlpha",
    )

    // Outer cyan/blue glow shadow
    val shadowModifier = if (outerGlow || (isHovered && enabled)) {
        Modifier.shadow(
            elevation = if (outerGlow) 8.dp else 4.dp,
            shape = shape,
            ambientColor = Color(0x354CC9F0),
            spotColor = Color(0x452563EB),
        )
    } else {
        Modifier
    }

    return this
        .then(shadowModifier)
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
            alpha = if (enabled) 1.0f else 0.45f
        }
        .clip(shape)
        .drawWithContent {
            // 1. Base liquid gradient fill
            val baseBrush = when (variant) {
                LiquidWaterVariant.ELECTRIC_BLUE -> DhunloGlassTokens.SelectedWaterGradient
                LiquidWaterVariant.PRIMARY_PLAY -> DhunloGlassTokens.primaryPlayWaterGradient(isDark)
                LiquidWaterVariant.SMOKY_OBSIDIAN -> DhunloGlassTokens.SmokyObsidianWaterGradient
                LiquidWaterVariant.FROSTED_PEARL -> DhunloGlassTokens.FrostedPearlWaterGradient
            }
            drawRect(brush = baseBrush)

            // 2. Dynamic water refraction sweep (blue/cyan light scatter at lower curvature)
            val refractionBrush = if (isDark || variant == LiquidWaterVariant.ELECTRIC_BLUE) {
                DhunloGlassTokens.DarkRefractionBrush
            } else {
                DhunloGlassTokens.LightRefractionBrush
            }
            val sweepHeight = (size.height * 0.45f).coerceAtLeast(1f)
            drawRect(
                brush = refractionBrush,
                topLeft = Offset(0f, size.height - sweepHeight),
                size = Size(size.width, sweepHeight),
            )

            // 3. Hover illumination boost
            if (hoverAlpha > 0f) {
                val hoverTint = when (variant) {
                    LiquidWaterVariant.ELECTRIC_BLUE -> Color(0x3838BDF8)
                    LiquidWaterVariant.PRIMARY_PLAY -> Color(0x304CC9F0)
                    LiquidWaterVariant.SMOKY_OBSIDIAN -> Color(0x254CC9F0)
                    LiquidWaterVariant.FROSTED_PEARL -> Color(0x2538BDF8)
                }
                drawRect(
                    color = hoverTint.copy(alpha = hoverTint.alpha * hoverAlpha),
                    topLeft = Offset.Zero,
                    size = size,
                )
            }

            // 4. Micro water bubbles with glassy glints and harmonic drift
            if (showBubbles && size.width > 12f && size.height > 12f) {
                val density = this
                for (bubble in DefaultMicroBubbles) {
                    val driftX = if (!reducedMotion) sin(driftCycle + bubble.phase) * 2.0f else 0f
                    val driftY = if (!reducedMotion) cos(driftCycle + bubble.phase) * 1.5f else 0f
                    val bx = size.width * bubble.relX + driftX
                    val by = size.height * bubble.relY + driftY
                    val r = bubble.radiusDp * density.density

                    if (bx - r > 0 && bx + r < size.width && by - r > 0 && by + r < size.height) {
                        val bubbleColor = when (variant) {
                            LiquidWaterVariant.ELECTRIC_BLUE -> Color(0xFFE0F7FF)
                            LiquidWaterVariant.PRIMARY_PLAY -> if (isDark) Color(0xFFBEE3F8) else Color.White
                            LiquidWaterVariant.SMOKY_OBSIDIAN -> Color(0xFFBAE6FD)
                            LiquidWaterVariant.FROSTED_PEARL -> Color(0xFF64748B)
                        }
                        val bubbleAlpha = (bubble.baseAlpha * if (isHovered) 1.25f else 1.0f).coerceAtMost(1f)

                        // Bubble inner water tint
                        drawCircle(
                            color = bubbleColor.copy(alpha = bubbleAlpha * 0.25f),
                            radius = r,
                            center = Offset(bx, by),
                        )
                        // Bubble outer reflective ring
                        drawCircle(
                            color = bubbleColor.copy(alpha = bubbleAlpha * 0.70f),
                            radius = r,
                            center = Offset(bx, by),
                            style = Stroke(width = 0.75f * density.density),
                        )
                        // Specular glint dot (top-left light glint)
                        drawCircle(
                            color = Color.White.copy(alpha = bubbleAlpha * 0.95f),
                            radius = (r * 0.35f).coerceAtLeast(0.75f),
                            center = Offset(bx - r * 0.35f, by - r * 0.35f),
                        )
                    }
                }
            }

            // 5. Interaction-driven expanding water ripple on click
            if (rippleProgress.value < 1f) {
                val p = rippleProgress.value
                val maxRadius = sqrt(size.width * size.width + size.height * size.height) * 0.6f
                val currentRadius = maxRadius * p
                val rippleAlpha = (1f - p) * 0.40f
                val rippleColor = when (variant) {
                    LiquidWaterVariant.ELECTRIC_BLUE -> Color(0xFF80E5FF)
                    LiquidWaterVariant.PRIMARY_PLAY -> Color(0xFF4CC9F0)
                    LiquidWaterVariant.SMOKY_OBSIDIAN -> Color(0xFF4CC9F0)
                    LiquidWaterVariant.FROSTED_PEARL -> Color(0xFF38BDF8)
                }
                drawCircle(
                    color = rippleColor.copy(alpha = rippleAlpha),
                    radius = currentRadius,
                    center = Offset(size.width / 2f, size.height / 2f),
                    style = Stroke(width = 2.dp.toPx() * (1f - p * 0.5f)),
                )
            }

            // 6. Top curved meniscus reflection arc
            val meniscusHeight = (size.height * 0.42f).coerceAtLeast(4f)
            val topReflectionAlpha = when (variant) {
                LiquidWaterVariant.ELECTRIC_BLUE -> 0.32f
                LiquidWaterVariant.PRIMARY_PLAY -> if (isDark) 0.35f else 0.45f
                LiquidWaterVariant.SMOKY_OBSIDIAN -> 0.20f
                LiquidWaterVariant.FROSTED_PEARL -> 0.38f
            } * (if (isHovered) 1.25f else 1.0f).coerceAtMost(1f)

            val meniscusBrush = Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = topReflectionAlpha),
                0.5f to Color.White.copy(alpha = topReflectionAlpha * 0.3f),
                1.0f to Color.Transparent,
                startY = 0f,
                endY = meniscusHeight,
            )
            drawRect(
                brush = meniscusBrush,
                topLeft = Offset.Zero,
                size = Size(size.width, meniscusHeight),
            )

            // 7. Render child content (icons, text) with complete clarity
            drawContent()

            // 8. Directional 135° specular rim border
            if (specularIntensity > 0f) {
                val borderBrush = when (variant) {
                    LiquidWaterVariant.ELECTRIC_BLUE -> Brush.linearGradient(
                        0.0f to Color(0xFF80E5FF).copy(alpha = (0.90f * specularIntensity).coerceAtMost(1f)),
                        0.5f to Color(0xFF2563EB).copy(alpha = (0.60f * specularIntensity).coerceAtMost(1f)),
                        1.0f to Color(0xFF003899).copy(alpha = (0.70f * specularIntensity).coerceAtMost(1f)),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                    LiquidWaterVariant.PRIMARY_PLAY -> Brush.linearGradient(
                        0.0f to Color.White.copy(alpha = ((if (isDark) 0.85f else 0.95f) * specularIntensity).coerceAtMost(1f)),
                        0.4f to Color(0xFF4CC9F0).copy(alpha = (0.55f * specularIntensity).coerceAtMost(1f)),
                        1.0f to Color(0x33000000),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                    LiquidWaterVariant.SMOKY_OBSIDIAN -> Brush.linearGradient(
                        0.0f to Color.White.copy(alpha = (0.65f * specularIntensity).coerceAtMost(1f)),
                        0.45f to Color(0x354CC9F0).copy(alpha = (0.40f * specularIntensity).coerceAtMost(1f)),
                        1.0f to Color(0x25000000),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                    LiquidWaterVariant.FROSTED_PEARL -> Brush.linearGradient(
                        0.0f to Color.White.copy(alpha = (0.90f * specularIntensity).coerceAtMost(1f)),
                        0.5f to Color(0xFF94A3B8).copy(alpha = (0.50f * specularIntensity).coerceAtMost(1f)),
                        1.0f to Color(0xFF64748B).copy(alpha = (0.40f * specularIntensity).coerceAtMost(1f)),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                }
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    brush = borderBrush,
                    style = Stroke(width = DhunloGlassTokens.BorderHairline.toPx()),
                )
            }
        }
}
