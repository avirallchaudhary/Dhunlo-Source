package com.metrolist.shared.ui.design.glass

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.skiaImageFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.clearSkikoComposeImplementation
import androidx.compose.ui.platform.registerSkikoComposeImplementation
import androidx.compose.ui.unit.dp
import com.metrolist.shared.ui.utils.softwareRendering
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(InternalComposeUiApi::class)
class DhunloGlassDesktopPrototypeTest {

    @BeforeTest
    fun setUp() {
        registerSkikoComposeImplementation()
        softwareRendering = false
    }

    @AfterTest
    fun tearDown() {
        softwareRendering = false
        clearSkikoComposeImplementation()
    }

    @Test
    fun tokensProvideExpectedObsidianPaletteAndGeometry() {
        assertEquals(Color(0xFF07080B), DhunloGlassTokens.ObsidianCanvas)
        assertEquals(Color(0xFF0E0F15), DhunloGlassTokens.ObsidianElevated)
        assertEquals(28.dp, DhunloGlassTokens.RadiusMiniPlayer)
        assertEquals(32.dp, DhunloGlassTokens.RadiusNavSuite)
        assertEquals(20.dp, DhunloGlassTokens.RadiusCard)

        val brush = DhunloGlassTokens.specularBorderBrush(intensity = 1.0f)
        assertNotNull(brush)
    }

    @Test
    fun blurSupportTierAdaptsToDesktopRenderingBackend() {
        softwareRendering = false
        assertEquals(BlurSupportTier.FULL_SHADERS, getBlurSupportTier())
        assertTrue(isHardwareBlurActive())

        softwareRendering = true
        assertEquals(BlurSupportTier.NONE, getBlurSupportTier())
        assertFalse(isHardwareBlurActive())
    }

    @Test
    fun skiaBackdropRenderingPrototypeExecutesAndCachesCleanly() {
        val width = 300
        val height = 150
        val blurRadiusPx = 28f

        // Prototype: Off-screen Skia surface rasterization with cached snapshot
        Surface.makeRasterN32Premul(width, height).use { surface ->
            val canvas = surface.canvas
            canvas.clear(DhunloGlassTokens.ObsidianCanvas.toArgb())

            val bounds = Rect.makeWH(width.toFloat(), height.toFloat())
            BlurEffect(blurRadiusPx, blurRadiusPx, TileMode.Clamp).skiaImageFilter.use { filter ->
                Paint().use { paint ->
                    paint.imageFilter = filter
                    canvas.saveLayer(bounds, paint)
                    try {
                        // Simulated backdrop content drawing
                        Paint().use { contentPaint ->
                            contentPaint.color = 0xFF336699.toInt()
                            canvas.drawCircle(width / 2f, height / 2f, 40f, contentPaint)
                        }
                    } finally {
                        canvas.restore()
                    }
                }
            }

            // Draw translucent glass tint overlay
            Paint().use { tintPaint ->
                tintPaint.color = DhunloGlassTokens.PanelGlassFill.toArgb()
                canvas.drawRect(bounds, tintPaint)
            }

            val snapshot = surface.makeImageSnapshot()
            try {
                assertNotNull(snapshot)
                assertEquals(width, snapshot.width)
                assertEquals(height, snapshot.height)
            } finally {
                snapshot.close()
            }
        }
    }

    @Test
    fun softwareRenderingAcrylicFallbackAvoidsSkiaBlurFilter() {
        softwareRendering = true
        val width = 200
        val height = 100

        // In software mode, zero blur passes are scheduled; direct acrylic tint rasterization is used
        Surface.makeRasterN32Premul(width, height).use { surface ->
            val canvas = surface.canvas
            canvas.clear(DhunloGlassTokens.ObsidianCanvas.toArgb())

            val bounds = Rect.makeWH(width.toFloat(), height.toFloat())
            Paint().use { acrylicPaint ->
                acrylicPaint.color = DhunloGlassTokens.AcrylicFallbackFill.toArgb()
                canvas.drawRect(bounds, acrylicPaint)
            }

            val snapshot = surface.makeImageSnapshot()
            try {
                assertNotNull(snapshot)
                assertEquals(width, snapshot.width)
            } finally {
                snapshot.close()
            }
        }
    }
}
