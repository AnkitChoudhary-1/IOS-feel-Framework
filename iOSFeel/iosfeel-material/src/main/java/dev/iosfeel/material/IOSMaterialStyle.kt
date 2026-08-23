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
                blurRadius = 6.dp,
                tintAlpha = 0.10f
            )
        IOSMaterialStyle.Thin ->
            IOSResolvedMaterial(
                blurRadius = 10.dp,
                tintAlpha = 0.14f
            )
        IOSMaterialStyle.Regular ->
            IOSResolvedMaterial(
                blurRadius = 16.dp,
                tintAlpha = 0.20f
            )
        IOSMaterialStyle.Thick ->
            IOSResolvedMaterial(
                blurRadius = 22.dp,
                tintAlpha = 0.26f
            )
    }
}
