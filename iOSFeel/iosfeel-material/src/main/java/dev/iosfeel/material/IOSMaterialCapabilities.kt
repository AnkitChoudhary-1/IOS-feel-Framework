package dev.iosfeel.material

import android.os.Build

object IOSMaterialCapabilities {
    val supportsRenderEffect: Boolean
        get() = Build.VERSION.SDK_INT >= 31

    val isLegacyAndroid: Boolean
        get() = Build.VERSION.SDK_INT < 31
}
