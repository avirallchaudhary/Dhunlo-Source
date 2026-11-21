package com.metrolist.shared.ui.design.glass

/**
 * Categorizes the host platform's hardware blur and shader rendering capabilities.
 *
 * This tiering drives the Liquid Glass design system, guaranteeing 60+ FPS on all
 * supported hardware without heavy per-frame CPU bitmap re-filtering.
 */
enum class BlurSupportTier {
    /**
     * Legacy Android (API < 31, Android 8-11) or Desktop Software Rendering.
     * Hardware-accelerated runtime blurs are unavailable. Surfaces automatically
     * fall back to lightweight frosted acrylic tints with specular borders.
     */
    NONE,

    /**
     * Android 12 to 12L (API 31-32).
     * Hardware-accelerated [RenderEffect] blur is available, but AGSL [RuntimeShader]
     * is not yet supported. Uses standard GPU blur passes.
     */
    BASIC_BLUR,

    /**
     * Android 13+ (API 33+) with AGSL, or Windows Desktop with Skia/Skiko GPU acceleration.
     * Full progressive depth blurs, custom SkSL/AGSL shaders, and specular refractions.
     */
    FULL_SHADERS,
}

/**
 * Returns the current platform's active blur support tier.
 */
expect fun getBlurSupportTier(): BlurSupportTier

/**
 * Returns true if the device currently supports and can execute hardware-accelerated blur.
 */
expect fun isHardwareBlurActive(): Boolean
