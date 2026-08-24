package dev.iosfeel.motion.morph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlin.math.roundToInt

/**
 * Physical morphing surface container that smoothly interpolates dimensions, position, and corner radii.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSMorphSurface(
    bounds: IOSMorphBounds,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 4.dp,
    containerColor: Color = Color(0xFF1C1C1E),
    backdrop: IOSBackdropState? = null,
    material: Boolean = false,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    val widthDp = with(density) { bounds.width.toDp() }
    val heightDp = with(density) { bounds.height.toDp() }
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceModifier = modifier
        .offset { IntOffset(x = bounds.left.roundToInt(), y = bounds.top.roundToInt()) }
        .size(width = widthDp, height = heightDp)
        .shadow(elevation = elevation, shape = shape)
        .clip(shape)

    if (material) {
        IOSMaterialSurface(
            backdrop = backdrop,
            config = IOSMaterialConfig(
                style = IOSMaterialStyle.Regular,
                cornerRadius = cornerRadius
            ),
            modifier = surfaceModifier
        ) {
            content()
        }
    } else {
        Box(
            modifier = surfaceModifier.background(containerColor)
        ) {
            content()
        }
    }
}
