package dev.iosfeel.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.core.tokens.IOSShapes
import dev.iosfeel.core.tokens.IOSSpacing

@Composable
fun IOSListSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    footer: String? = null,
    containerColor: Color = Color(0xFF1C1C1E),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = IOSSpacing.Small)
    ) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8E8E93),
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(
                    start = IOSSpacing.Large,
                    bottom = IOSSpacing.Small
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(IOSShapes.Card)
                .background(containerColor),
            content = content
        )

        if (footer != null) {
            Text(
                text = footer,
                fontSize = 12.sp,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(
                    start = IOSSpacing.Large,
                    top = IOSSpacing.Small,
                    end = IOSSpacing.Large
                )
            )
        }
    }
}
