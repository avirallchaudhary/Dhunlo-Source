package com.metrolist.shared.ui.design.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

/**
 * Centralized branding assets and identity for DHUNLO.
 *
 * All user-facing brand marks, product titles, and logo presentation
 * route through this component to ensure seamless future asset replacement.
 */
object DhunloBranding {
    const val PRODUCT_NAME = "DHUNLO"
    const val PRODUCT_DISPLAY_NAME = "Dhunlo"
    const val TAGLINE = "Pure Audio Experience"

    /** Primary gradient accent for the brand disc. */
    val BrandGradient =
        Brush.linearGradient(
            listOf(
                Color(0xFF8A56FF), // Luminous electric violet
                Color(0xFF4361EE), // Deep liquid blue
                Color(0xFF4CC9F0), // Bright cyan glow
            ),
        )
}

/**
 * Centralized brand icon / logo mark for DHUNLO.
 * Renders the official liquid glass music note logo with a subtle specular glass border.
 */
@Composable
fun DhunloBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .glassBorder(
                    shape = CircleShape,
                    borderWidth = DhunloGlassTokens.BorderHairline,
                    intensity = 1.3f,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = DhunloBranding.PRODUCT_DISPLAY_NAME,
            modifier = Modifier.size(size).clip(CircleShape),
        )
    }
}
