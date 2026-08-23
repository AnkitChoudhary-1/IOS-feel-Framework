package dev.iosfeel.dayline.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun DaylineTabIcon(
    tab: DaylineTab,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color
) {
    val tint = if (selected) activeColor else inactiveColor

    when (tab) {
        DaylineTab.Today -> {
            Icon(
                imageVector = if (selected) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                contentDescription = "Today",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        DaylineTab.Plan -> {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = "Plan",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        DaylineTab.Capture -> {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(activeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        DaylineTab.Insights -> {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Insights",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        DaylineTab.You -> {
            Icon(
                imageVector = if (selected) Icons.Filled.Person else Icons.Outlined.Person,
                contentDescription = "You",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
