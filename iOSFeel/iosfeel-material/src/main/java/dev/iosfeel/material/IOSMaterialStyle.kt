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
                blurRadius = 14.dp,
                tintAlpha = 0.08f
            )
        IOSMaterialStyle.Thin ->
            IOSResolvedMaterial(
                blurRadius = 20.dp,
                tintAlpha = 0.11f
            )
        IOSMaterialStyle.Regular ->
            IOSResolvedMaterial(
                blurRadius = 28.dp,
                tintAlpha = 0.15f
            )
        IOSMaterialStyle.Thick ->
            IOSResolvedMaterial(
                blurRadius = 38.dp,
                tintAlpha = 0.20f
            )
    }
}
