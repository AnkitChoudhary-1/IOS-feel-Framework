package dev.iosfeel.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.core.tokens.IOSShapes
import dev.iosfeel.core.tokens.IOSSpacing

object IOSButtonDefaults {
    val Shape: RoundedCornerShape = IOSShapes.Button
    val CornerRadius: Dp = 14.dp
    val MinHeight: Dp = 50.dp
    val SmallMinHeight: Dp = 36.dp
    val ContentPadding = PaddingValues(horizontal = IOSSpacing.Large, vertical = IOSSpacing.Medium)

    val PressConfig: IOSPressConfig = IOSPressConfig(pressedScale = 0.975f)

    val PrimaryFilledColor = Color(0xFF007AFF)
    val PrimaryContentColor = Color.White

    val TintedContainerColor = Color(0xFF007AFF).copy(alpha = 0.15f)
    val TintedContentColor = Color(0xFF007AFF)

    val MaterialContentColor = Color.White

    val PlainContentColor = Color(0xFF007AFF)

    fun containerColorFor(style: IOSButtonStyle): Color {
        return when (style) {
            IOSButtonStyle.Filled -> PrimaryFilledColor
            IOSButtonStyle.Tinted -> TintedContainerColor
            IOSButtonStyle.Material -> Color.Transparent
            IOSButtonStyle.Plain -> Color.Transparent
        }
    }

    fun contentColorFor(style: IOSButtonStyle): Color {
        return when (style) {
            IOSButtonStyle.Filled -> PrimaryContentColor
            IOSButtonStyle.Tinted -> TintedContentColor
            IOSButtonStyle.Material -> MaterialContentColor
            IOSButtonStyle.Plain -> PlainContentColor
        }
    }
}
