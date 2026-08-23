package dev.iosfeel.dayline.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class DaylineTab(
    val title: String,
    val isAction: Boolean = false
) {
    Today("Today"),
    Plan("Plan"),
    Capture("Add", isAction = true),
    Insights("Insights"),
    You("You")
}
