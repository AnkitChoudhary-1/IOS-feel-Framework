package dev.iosfeel.components.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.iosfeel.core.tokens.IOSShapes

@Immutable
data class IOSFeelShapes(
    val control: RoundedCornerShape = IOSShapes.Control,
    val button: RoundedCornerShape = IOSShapes.Button,
    val card: RoundedCornerShape = IOSShapes.Card,
    val largeSurface: RoundedCornerShape = IOSShapes.LargeSurface,
    val sheet: RoundedCornerShape = IOSShapes.Sheet,
    val pill: RoundedCornerShape = IOSShapes.Pill
) {
    companion object {
        val Default = IOSFeelShapes()
    }
}

val LocalIOSFeelShapes = staticCompositionLocalOf {
    IOSFeelShapes.Default
}
