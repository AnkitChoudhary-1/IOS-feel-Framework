package dev.iosfeel.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.theme.IOSFeelTheme

@Composable
fun IOSBadge(
    count: Int? = null,
    modifier: Modifier = Modifier,
    color: Color = IOSFeelTheme.colors.destructive
) {
    val label = when {
        count == null -> null
        count > 99 -> "99+"
        else -> count.toString()
    }

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = if (label == null) 8.dp else 18.dp,
                minHeight = if (label == null) 8.dp else 18.dp
            )
            .background(
                color = color,
                shape = CircleShape
            )
            .padding(
                horizontal = if (label == null) 0.dp else 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        label?.let {
            Text(
                text = it,
                style = IOSFeelTheme.typography.badge,
                color = Color.White
            )
        }
    }
}
