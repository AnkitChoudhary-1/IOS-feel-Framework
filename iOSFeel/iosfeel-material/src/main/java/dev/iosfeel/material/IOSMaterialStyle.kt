package dev.iosfeel.material

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IOSMaterialStyle {
    UltraThin,
    Thin,
    Regular,
    Thick
}

data class IOSResolvedMaterial(
    val blurRadius: Dp,
    val tintAlpha: Float
)

fun resolveIOSMaterial(
    style: IOSMaterialStyle
): IOSResolvedMaterial {
    return when (style) {
        IOSMaterialStyle.UltraThin ->
            IOSResolvedMaterial(
                blurRadius = 10.dp,
                tintAlpha = 0.55f
            )
        IOSMaterialStyle.Thin ->
            IOSResolvedMaterial(
                blurRadius = 16.dp,
                tintAlpha = 0.68f
            )
        IOSMaterialStyle.Regular ->
            IOSResolvedMaterial(
                blurRadius = 24.dp,
                tintAlpha = 0.78f
            )
        IOSMaterialStyle.Thick ->
            IOSResolvedMaterial(
                blurRadius = 32.dp,
                tintAlpha = 0.88f
            )
    }
}
