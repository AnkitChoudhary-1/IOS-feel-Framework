package dev.iosfeel.components.floatingbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
data class IOSFloatingTabItem<T>(
    val value: T,
    val label: String? = null,
    val badgeCount: Int? = null,
    val showBadgeDot: Boolean = false,
    val icon: @Composable (selected: Boolean) -> Unit
)
