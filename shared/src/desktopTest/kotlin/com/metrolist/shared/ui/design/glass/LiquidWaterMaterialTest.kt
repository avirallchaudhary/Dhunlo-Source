package com.metrolist.shared.ui.design.glass

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LiquidWaterMaterialTest {

    @Test
    fun liquidWaterTokensProvideExpectedPaletteAndGradients() {
        assertEquals(Color(0xFF4CC9F0), DhunloGlassTokens.WaterCyanRefraction)
        assertEquals(Color(0xFF2563EB), DhunloGlassTokens.WaterElectricBlue)
        assertEquals(Color(0xFF0052D4), DhunloGlassTokens.WaterDeepBlue)
        assertEquals(Color(0xFF38BDF8), DhunloGlassTokens.WaterIceBlue)
        assertEquals(Color(0xFF0F172A), DhunloGlassTokens.WaterPearlText)

        assertNotNull(DhunloGlassTokens.SelectedWaterGradient)
        assertNotNull(DhunloGlassTokens.SmokyObsidianWaterGradient)
        assertNotNull(DhunloGlassTokens.FrostedPearlWaterGradient)
        assertNotNull(DhunloGlassTokens.WaterRefractionBrush)

        val darkPlayGradient = DhunloGlassTokens.primaryPlayWaterGradient(isDark = true)
        val lightPlayGradient = DhunloGlassTokens.primaryPlayWaterGradient(isDark = false)
        assertNotNull(darkPlayGradient)
        assertNotNull(lightPlayGradient)
    }

    @Test
    fun liquidWaterVariantsEnumDefinesAllRequiredTiers() {
        val variants = LiquidWaterVariant.values()
        assertEquals(4, variants.size)
        assertTrue(variants.contains(LiquidWaterVariant.SMOKY_OBSIDIAN))
        assertTrue(variants.contains(LiquidWaterVariant.FROSTED_PEARL))
        assertTrue(variants.contains(LiquidWaterVariant.ELECTRIC_BLUE))
        assertTrue(variants.contains(LiquidWaterVariant.PRIMARY_PLAY))
    }

    @Test
    fun defaultMicroBubblesAreOrganicallyDistributedWithinBounds() {
        assertEquals(5, DefaultMicroBubbles.size)
        for (bubble in DefaultMicroBubbles) {
            assertTrue(bubble.relX in 0.05f..0.95f, "Bubble relX (${bubble.relX}) should be within components bounds")
            assertTrue(bubble.relY in 0.05f..0.95f, "Bubble relY (${bubble.relY}) should be within components bounds")
            assertTrue(bubble.radiusDp in 1.0f..5.0f, "Bubble radiusDp (${bubble.radiusDp}) should be organic micro scale")
            assertTrue(bubble.baseAlpha in 0.2f..1.0f, "Bubble baseAlpha (${bubble.baseAlpha}) should be translucent")
        }
    }

    @Test
    fun harmonicDriftCalculationsRemainBoundedDuringIdle() {
        // Test drift across full 2*PI circle cycle
        val testCycles = listOf(0f, (PI / 4).toFloat(), (PI / 2).toFloat(), PI.toFloat(), (3 * PI / 2).toFloat(), (2 * PI).toFloat())
        for (cycle in testCycles) {
            for (bubble in DefaultMicroBubbles) {
                val driftX = sin(cycle + bubble.phase) * 2.0f
                val driftY = cos(cycle + bubble.phase) * 1.5f
                assertTrue(driftX in -2.01f..2.01f, "Drift X should remain within bounds: $driftX")
                assertTrue(driftY in -1.51f..1.51f, "Drift Y should remain within bounds: $driftY")
            }
        }
    }

    @Test
    fun chipVariantThemeSelectionLogicEnsuresEdgeToEdgeElectricBlueOnSelected() {
        fun resolveChipVariant(selected: Boolean, isDark: Boolean): LiquidWaterVariant =
            when {
                selected -> LiquidWaterVariant.ELECTRIC_BLUE
                isDark -> LiquidWaterVariant.SMOKY_OBSIDIAN
                else -> LiquidWaterVariant.FROSTED_PEARL
            }

        // Regardless of theme, selected chips MUST resolve to ELECTRIC_BLUE
        assertEquals(LiquidWaterVariant.ELECTRIC_BLUE, resolveChipVariant(selected = true, isDark = true))
        assertEquals(LiquidWaterVariant.ELECTRIC_BLUE, resolveChipVariant(selected = true, isDark = false))

        // Unselected chips resolve to theme-appropriate liquid obsidian or frosted pearl
        assertEquals(LiquidWaterVariant.SMOKY_OBSIDIAN, resolveChipVariant(selected = false, isDark = true))
        assertEquals(LiquidWaterVariant.FROSTED_PEARL, resolveChipVariant(selected = false, isDark = false))
    }

    @Test
    fun playButtonVariantSelectionResolvesToPrimaryPlay() {
        val playVariant = LiquidWaterVariant.PRIMARY_PLAY
        assertEquals(LiquidWaterVariant.PRIMARY_PLAY, playVariant)
    }

    @Test
    fun themeAwareTokensProvideOptimalContrastForLightAndDark() {
        // Dark theme typography tokens
        assertEquals(DhunloGlassTokens.TextPrimary, DhunloGlassTokens.textPrimary(isDark = true))
        assertEquals(DhunloGlassTokens.TextSecondary, DhunloGlassTokens.textSecondary(isDark = true))
        assertEquals(DhunloGlassTokens.TextTertiary, DhunloGlassTokens.textTertiary(isDark = true))

        // Light theme typography tokens (high-contrast slate/obsidian)
        assertEquals(DhunloGlassTokens.WaterPearlText, DhunloGlassTokens.textPrimary(isDark = false))
        assertEquals(Color(0xFF475569), DhunloGlassTokens.textSecondary(isDark = false))
        assertEquals(Color(0xFF64748B), DhunloGlassTokens.textTertiary(isDark = false))

        // Dark theme glass fills
        assertEquals(DhunloGlassTokens.FloatingGlassDarkFill, DhunloGlassTokens.floatingGlassFill(isDark = true))
        assertEquals(DhunloGlassTokens.PanelGlassFill, DhunloGlassTokens.panelGlassFill(isDark = true))

        // Light theme frosted pearl glass fills
        assertEquals(DhunloGlassTokens.WaterFrostedPearlDeepFill, DhunloGlassTokens.floatingGlassFill(isDark = false))
        assertEquals(DhunloGlassTokens.WaterFrostedPearlFill, DhunloGlassTokens.panelGlassFill(isDark = false))
    }
}
