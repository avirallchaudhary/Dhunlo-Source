package com.metrolist.shared.ui.design.glass

import android.os.Build

actual fun getBlurSupportTier(): BlurSupportTier =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> BlurSupportTier.FULL_SHADERS
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> BlurSupportTier.BASIC_BLUR
        else -> BlurSupportTier.NONE
    }

actual fun isHardwareBlurActive(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
