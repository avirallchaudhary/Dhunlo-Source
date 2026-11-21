package com.metrolist.shared.ui.design.glass

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable design tokens for the DHUNLO iPhone-inspired Liquid Glass design system.
 *
 * Implements a layered optical depth model with deep obsidian foundations,
 * translucent glass surface tiers, continuous squircle geometry, and directional
 * specular highlights.
 */
object DhunloGlassTokens {

    // ========================================================================
    // 1. Obsidian Background Foundations
    // ========================================================================

    /** Base canvas background: rich, deep obsidian black-blue for maximum OLED contrast. */
    val ObsidianCanvas = Color(0xFF07080B)

    /** Slightly elevated obsidian for docked rails or layered background sections. */
    val ObsidianElevated = Color(0xFF0E0F15)

    /** Pure deep void obsidian for extreme contrast. */
    val ObsidianDeep = Color(0xFF030305)

    /** Subtle 40% dark scrim for softening bright background elements. */
    val ObsidianScrimSubtle = Color(0x66000000)

    /** Standard 60% dark scrim for artwork backdrops, maintaining WCAG contrast. */
    val ObsidianScrimStandard = Color(0x99000000)

    /** Heavy 85% dark scrim for ultra-vibrant artwork, guaranteeing readability. */
    val ObsidianScrimHeavy = Color(0xD9000000)

    // ========================================================================
    // 2. Glass Surface Colors (Elevation Tiers)
    // ========================================================================

    /** Tier 1: Frosted panel glass fill (~6% white). Ideal for content cards, sheets, lists. */
    val PanelGlassFill = Color(0x0FFFFFFF)

    /** Tier 2: Translucent floating glass fill (~10% white). For dialogs and floating menus. */
    val FloatingGlassFill = Color(0x1AFFFFFF)

    /** Tier 2: Dark refractive floating island fill (~75% dark obsidian). For Floating MiniPlayer & NavSuite. */
    val FloatingGlassDarkFill = Color(0xC012131A)

    /** Tier 3: Interactive glass control fill (~18% white). For media buttons, pill toggles. */
    val ControlGlassFill = Color(0x2EFFFFFF)

    /** Tier 3: Primary Play/Pause glass surface fill (~23% white with crystalline clarity). */
    val PlayGlassFill = Color(0x3BFFFFFF)

    /** Primary Play/Pause text & icon color (100% pure crisp white). */
    val PlayGlassContent = Color(0xFFFFFFFF)

    /** Secondary playback control button fill (~13% white). For Previous/Next controls. */
    val SideControlGlassFill = Color(0x22FFFFFF)

    /** Tier 3: Active prominent pill fill (100% white). For primary play buttons and active pills. */
    val ActivePillFill = Color(0xFFFFFFFF)

    /** Content color placed on top of [ActivePillFill]. */
    val ActivePillContent = Color(0xFF07080B)

    /** Fallback high-opacity dark acrylic (92%) for legacy Android (API < 31) and software desktop. */
    val AcrylicFallbackFill = Color(0xEB12131A)

    /** Fallback card acrylic (84%) for legacy Android (API < 31) and software desktop. */
    val AcrylicPanelFallbackFill = Color(0xD61A1B24)

    // ========================================================================
    // 3. Transparency & Opacity Levels
    // ========================================================================

    const val OpacitySubtle = 0.06f
    const val OpacityMedium = 0.12f
    const val OpacityPill = 0.18f
    const val OpacityProminent = 0.26f
    const val OpacityDisabled = 0.38f
    const val OpacitySecondary = 0.65f
    const val OpacityPrimary = 1.00f

    // ========================================================================
    // 4. Borders & Specular Highlights
    // ========================================================================

    val BorderHairline: Dp = 0.75.dp
    val BorderStandard: Dp = 1.0.dp
    val BorderFocus: Dp = 2.0.dp

    private fun createSpecularBrush(intensity: Float, startAlpha: Float = 0.28f, midAlpha: Float = 0.10f, endAlpha: Float = 0.03f): Brush =
        Brush.linearGradient(
            0.00f to Color.White.copy(alpha = (startAlpha * intensity).coerceIn(0f, 1f)),
            0.45f to Color.White.copy(alpha = (midAlpha * intensity).coerceIn(0f, 1f)),
            1.00f to Color.White.copy(alpha = (endAlpha * intensity).coerceIn(0f, 1f)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )

    private val SpecularBorder100: Brush = createSpecularBrush(1.00f)
    private val SpecularBorder85: Brush = createSpecularBrush(0.85f)
    private val SpecularBorder80: Brush = createSpecularBrush(0.80f)
    private val SpecularBorder65: Brush = createSpecularBrush(0.65f)
    private val SpecularBorder60: Brush = createSpecularBrush(0.60f)

    val DarkRefractionBrush: Brush =
        Brush.horizontalGradient(
            0.0f to Color.Transparent,
            0.5f to Color(0x2E4CC9F0),
            1.0f to Color.Transparent,
        )

    val LightRefractionBrush: Brush =
        Brush.horizontalGradient(
            0.0f to Color.Transparent,
            0.5f to Color(0x2238BDF8),
            1.0f to Color.Transparent,
        )

    /**
     * Standard 135° directional specular reflection brush.
     * Simulates light hitting the top-left edge of a refractive glass sheet.
     * Reuses pre-created instances for common intensities to avoid draw-time allocations.
     */
    fun specularBorderBrush(
        intensity: Float = 1.0f,
        startAlpha: Float = 0.28f,
        midAlpha: Float = 0.10f,
        endAlpha: Float = 0.03f,
    ): Brush {
        if (startAlpha == 0.28f && midAlpha == 0.10f && endAlpha == 0.03f) {
            when (intensity) {
                1.0f -> return SpecularBorder100
                0.85f -> return SpecularBorder85
                0.8f -> return SpecularBorder80
                0.65f -> return SpecularBorder65
                0.6f -> return SpecularBorder60
            }
        }
        return createSpecularBrush(intensity, startAlpha, midAlpha, endAlpha)
    }

    /**
     * Active specular reflection brush that blends white edge highlights with the artwork accent.
     */
    fun activeSpecularBorderBrush(
        accentColor: Color,
        intensity: Float = 1.0f,
    ): Brush =
        Brush.linearGradient(
            0.00f to Color.White.copy(alpha = (0.45f * intensity).coerceIn(0f, 1f)),
            0.40f to accentColor.copy(alpha = (0.35f * intensity).coerceIn(0f, 1f)),
            1.00f to accentColor.copy(alpha = (0.12f * intensity).coerceIn(0f, 1f)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )

    /**
     * Luminous brand specular highlight brush blending electric violet, cyan and specular white.
     */
    fun brandSpecularBorderBrush(intensity: Float = 1.0f): Brush =
        Brush.linearGradient(
            0.00f to Color(0xFF4CC9F0).copy(alpha = (0.50f * intensity).coerceIn(0f, 1f)),
            0.40f to Color(0xFF8A56FF).copy(alpha = (0.35f * intensity).coerceIn(0f, 1f)),
            1.00f to Color.White.copy(alpha = (0.12f * intensity).coerceIn(0f, 1f)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )

    /** Vertical specular divider line separating the frosted sidebar rail from the main canvas. */
    val RailDividerBrush: Brush =
        Brush.verticalGradient(
            0.00f to Color.White.copy(alpha = 0.22f),
            0.20f to Color(0xFF4CC9F0).copy(alpha = 0.16f),
            0.55f to Color.White.copy(alpha = 0.06f),
            1.00f to Color.White.copy(alpha = 0.08f),
        )

    /** High-contrast accessibility keyboard focus border. */
    val FocusBorderColor = Color.White.copy(alpha = 0.90f)

    // ========================================================================
    // 5. Corner Radii & Geometry (Continuous Squircles)
    // ========================================================================

    val RadiusChip: Dp = 12.dp
    val RadiusThumbnail: Dp = 16.dp
    val RadiusCard: Dp = 20.dp
    val RadiusThumbnailLarge: Dp = 24.dp
    val RadiusMiniPlayer: Dp = 28.dp
    val RadiusNavSuite: Dp = 32.dp
    val RadiusSheet: Dp = 32.dp
    val RadiusThumbnailHero: Dp = 32.dp

    val ShapeChip: Shape = RoundedCornerShape(RadiusChip)
    val ShapeThumbnail: Shape = RoundedCornerShape(RadiusThumbnail)
    val ShapeCard: Shape = RoundedCornerShape(RadiusCard)
    val ShapeThumbnailLarge: Shape = RoundedCornerShape(RadiusThumbnailLarge)
    val ShapeMiniPlayer: Shape = RoundedCornerShape(RadiusMiniPlayer)
    val ShapeNavSuite: Shape = RoundedCornerShape(RadiusNavSuite)
    val ShapeSheet: Shape = RoundedCornerShape(topStart = RadiusSheet, topEnd = RadiusSheet)
    val ShapeThumbnailHero: Shape = RoundedCornerShape(RadiusThumbnailHero)
    val ShapePill: Shape = CircleShape

    // ========================================================================
    // 6. Typography & Text Hierarchy
    // ========================================================================

    val TextPrimary = Color(0xFFF7F8FA)
    val TextSecondary = Color(0xFFA0A3AD)
    val TextTertiary = Color(0xFF6B7080)

    val DisplayLarge =
        TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.6).sp,
            color = TextPrimary,
        )

    val HeadlineMedium =
        TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.4).sp,
            color = TextPrimary,
        )

    val TitleMedium =
        TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.2).sp,
            color = TextPrimary,
        )

    val BodyMedium =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
            color = TextSecondary,
        )

    val LabelSmall =
        TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.3.sp,
            color = TextTertiary,
        )

    // ========================================================================
    // 7. Spacing Grid (4/8-point standard)
    // ========================================================================

    val Space2xs: Dp = 4.dp
    val SpaceXs: Dp = 8.dp
    val SpaceSm: Dp = 12.dp
    val SpaceMd: Dp = 16.dp
    val SpaceLg: Dp = 24.dp
    val SpaceXl: Dp = 32.dp
    val Space2xl: Dp = 48.dp

    // ========================================================================
    // 8. Elevation, Depth & Blur Radius
    // ========================================================================

    val BlurSubtle: Dp = 16.dp
    val BlurPanel: Dp = 28.dp
    val BlurIsland: Dp = 44.dp
    val BlurAura: Dp = 64.dp

    val ElevationNone: Dp = 0.dp
    val ElevationSubtle: Dp = 4.dp
    val ElevationMedium: Dp = 8.dp
    val ElevationFloating: Dp = 16.dp

    val ShadowAmbient = Color(0x73000000)

    // ========================================================================
    // 9. Liquid Obsidian & Dynamic Water Refraction Tokens (Stage 1)
    // ========================================================================

    /** Deep translucent smoky obsidian glass fill for interactive controls and chips (~21% opacity). */
    val WaterSmokyObsidianFill = Color(0x350E0F15)

    /** Slightly deeper smoky obsidian for layered depth (~33% opacity). */
    val WaterSmokyObsidianDeepFill = Color(0x5507080B)

    /** Translucent pearl silver/white glass fill for light theme (Frosted Pearl) (~27% opacity). */
    val WaterFrostedPearlFill = Color(0x45FFFFFF)

    /** Deeper pearl tint for light theme layered depth. */
    val WaterFrostedPearlDeepFill = Color(0x50E2E8F0)

    /** Primary electric cyan watery highlight. */
    val WaterCyanRefraction = Color(0xFF4CC9F0)

    /** Secondary luminous electric blue for full-bleed liquid saturation. */
    val WaterElectricBlue = Color(0xFF2563EB)

    /** Deep oceanic blue for bottom meniscus depth. */
    val WaterDeepBlue = Color(0xFF0052D4)

    /** Light theme ice-blue refraction tint. */
    val WaterIceBlue = Color(0xFF38BDF8)

    /** Light theme dark typography / icon color for high contrast against frosted pearl. */
    val WaterPearlText = Color(0xFF0F172A)

    /**
     * Full-bleed electric-blue liquid-glass gradient for selected chips and active states.
     * Transitions from radiant cyan at the top to electric blue in the center to deep oceanic blue at the base.
     */
    val SelectedWaterGradient: Brush =
        Brush.verticalGradient(
            listOf(
                Color(0xFF38BDF8), // Radiant cyan top
                Color(0xFF2563EB), // Rich electric blue center
                Color(0xFF0052D4), // Deep liquid blue base
            ),
        )

    /**
     * Translucent smoky liquid glass gradient for dark theme (Liquid Obsidian).
     */
    val SmokyObsidianWaterGradient: Brush =
        Brush.verticalGradient(
            listOf(
                Color(0x38FFFFFF), // Specular light rim
                Color(0x1812131A), // Smoky translucent body
                Color(0x3207080B), // Deep obsidian base
            ),
        )

    /**
     * Translucent frosted pearl glass gradient for light theme (Frosted Pearl).
     */
    val FrostedPearlWaterGradient: Brush =
        Brush.verticalGradient(
            listOf(
                Color(0x75FFFFFF), // Crisp pearl specular rim
                Color(0x35E2E8F0), // Translucent silver body
                Color(0x48CBD5E1), // Frosted silver base
            ),
        )

    /**
     * Horizontal refraction sweep brush for water light scattering.
     */
    val WaterRefractionBrush: Brush =
        Brush.horizontalGradient(
            listOf(
                Color(0x004CC9F0),
                Color(0x2E4CC9F0),
                Color(0x004CC9F0),
            ),
        )

    /**
     * Luminous radial refraction for primary Play/Pause liquid button.
     */
    fun primaryPlayWaterGradient(isDark: Boolean): Brush =
        Brush.radialGradient(
            if (isDark) {
                listOf(
                    Color(0x404CC9F0), // Internal cyan glow
                    Color(0x281E293B), // Translucent slate
                    Color(0x380F172A), // Deep obsidian rim
                )
            } else {
                listOf(
                    Color(0x3538BDF8), // Ice blue core
                    Color(0x35FFFFFF), // Luminous pearl
                    Color(0x45E2E8F0), // Frosted rim
                )
            },
        )

    // ========================================================================
    // 10. Theme-Aware Adaptive Color Helpers
    // ========================================================================

    /**
     * Resolves high-contrast primary typography color based on current theme.
     * Dark Theme (Liquid Obsidian): pure radiant white (#F7F8FA)
     * Light Theme (Frosted Pearl): deep slate obsidian (#0F172A)
     */
    fun textPrimary(isDark: Boolean): Color =
        if (isDark) TextPrimary else WaterPearlText

    /**
     * Resolves secondary typography color based on current theme.
     * Dark Theme: muted silver (#A0A3AD)
     * Light Theme: dark slate secondary (#475569)
     */
    fun textSecondary(isDark: Boolean): Color =
        if (isDark) TextSecondary else Color(0xFF475569)

    /**
     * Resolves tertiary typography color based on current theme.
     * Dark Theme: deep silver (#64748B)
     * Light Theme: medium slate (#64748B)
     */
    fun textTertiary(isDark: Boolean): Color =
        if (isDark) TextTertiary else Color(0xFF64748B)

    /**
     * Resolves floating surface fill color based on current theme.
     * Dark Theme: dark obsidian FloatingGlassDarkFill (#C012131A)
     * Light Theme: translucent pearl WaterFrostedPearlDeepFill (#50E2E8F0)
     */
    fun floatingGlassFill(isDark: Boolean): Color =
        if (isDark) FloatingGlassDarkFill else WaterFrostedPearlDeepFill

    /**
     * Resolves panel glass fill color based on current theme.
     * Dark Theme: dark PanelGlassFill (#0FFFFFFF)
     * Light Theme: translucent pearl WaterFrostedPearlFill (#45FFFFFF)
     */
    fun panelGlassFill(isDark: Boolean): Color =
        if (isDark) PanelGlassFill else WaterFrostedPearlFill
}
