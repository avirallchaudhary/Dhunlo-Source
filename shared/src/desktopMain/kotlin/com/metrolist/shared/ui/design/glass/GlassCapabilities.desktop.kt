package com.metrolist.shared.ui.design.glass

import com.metrolist.shared.ui.utils.softwareRendering

actual fun getBlurSupportTier(): BlurSupportTier =
    if (softwareRendering) {
        BlurSupportTier.NONE
    } else {
        BlurSupportTier.FULL_SHADERS
    }

actual fun isHardwareBlurActive(): Boolean = !softwareRendering
