package dev.iosfeel.components.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.iosfeel.core.tokens.IOSTypographyTokens

@Immutable
data class IOSFeelTypography(
    val largeTitle: TextStyle = IOSTypographyTokens.LargeTitle,
    val title1: TextStyle = IOSTypographyTokens.Title1,
    val title2: TextStyle = IOSTypographyTokens.Title2,
    val title3: TextStyle = IOSTypographyTokens.Title3,
    val headline: TextStyle = IOSTypographyTokens.Headline,
    val body: TextStyle = IOSTypographyTokens.Body,
    val callout: TextStyle = IOSTypographyTokens.Callout,
    val subheadline: TextStyle = IOSTypographyTokens.Subheadline,
    val footnote: TextStyle = IOSTypographyTokens.Footnote,
    val caption1: TextStyle = IOSTypographyTokens.Caption1,
    val caption2: TextStyle = IOSTypographyTokens.Caption2,
    val badge: TextStyle = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 13.sp
    )
) {
    companion object {
        val Default = IOSFeelTypography()
    }
}

val LocalIOSFeelTypography = staticCompositionLocalOf {
    IOSFeelTypography.Default
}
