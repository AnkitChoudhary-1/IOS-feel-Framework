package dev.iosfeel.sonora.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class SonoraTab {
    Home,
    Library,
    Search,
    Settings
}

@Composable
fun SonoraTabIcon(
    tab: SonoraTab,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    val color = if (selected) activeColor else inactiveColor

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (tab) {
            SonoraTab.Home -> {
                // House outline or filled
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.88f, h * 0.48f)
                    lineTo(w * 0.78f, h * 0.48f)
                    lineTo(w * 0.78f, h * 0.85f)
                    lineTo(w * 0.22f, h * 0.85f)
                    lineTo(w * 0.22f, h * 0.48f)
                    lineTo(w * 0.12f, h * 0.48f)
                    close()
                }
                if (selected) {
                    drawPath(path, color)
                } else {
                    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
                }
            }

            SonoraTab.Library -> {
                // Stack of albums / music notes
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.15f, h * 0.25f),
                    size = Size(w * 0.7f, h * 0.6f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = if (selected) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = strokeWidth)
                )
                // Music note lines
                drawLine(
                    color = if (selected) Color.White else color,
                    start = Offset(w * 0.3f, h * 0.45f),
                    end = Offset(w * 0.7f, h * 0.45f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = if (selected) Color.White else color,
                    start = Offset(w * 0.3f, h * 0.6f),
                    end = Offset(w * 0.55f, h * 0.6f),
                    strokeWidth = 2.dp.toPx()
                )
            }

            SonoraTab.Search -> {
                // Magnifying glass
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = color,
                    radius = w * 0.3f,
                    center = Offset(w * 0.42f, h * 0.42f),
                    style = if (selected) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = strokeWidth)
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.63f, h * 0.63f),
                    end = Offset(w * 0.85f, h * 0.85f),
                    strokeWidth = 2.5.dp.toPx()
                )
            }

            SonoraTab.Settings -> {
                // Gear / slider icon
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = color,
                    radius = w * 0.35f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = strokeWidth)
                )
                drawCircle(
                    color = color,
                    radius = w * 0.12f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = if (selected) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = strokeWidth)
                )
            }
        }
    }
}
