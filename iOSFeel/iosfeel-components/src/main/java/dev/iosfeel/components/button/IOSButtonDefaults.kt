package dev.iosfeel.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.core.tokens.IOSColors

object IOSButtonDefaults {

    val ContentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    val MinHeight: Dp = 50.dp
    val CornerRadius: Dp = 12.dp

    @Composable
    fun containerColorFor(style: IOSButtonStyle): Color = when (style) {
        IOSButtonStyle.Filled -> IOSColors.SystemBlue
        IOSButtonStyle.Tinted -> IOSColors.SystemBlue.copy(alpha = 0.15f)
        IOSButtonStyle.Material -> Color.Transparent
        IOSButtonStyle.Plain -> Color.Transparent
        IOSButtonStyle.Destructive -> IOSColors.SystemRed
    }

    @Composable
    fun contentColorFor(style: IOSButtonStyle): Color = when (style) {
        IOSButtonStyle.Filled -> Color.White
        IOSButtonStyle.Tinted -> IOSColors.SystemBlue
        IOSButtonStyle.Material -> IOSColors.SystemBlue
        IOSButtonStyle.Plain -> IOSColors.SystemBlue
        IOSButtonStyle.Destructive -> Color.White
    }
}
