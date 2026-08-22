package dev.iosfeel.navigation

import androidx.compose.runtime.Immutable

@Immutable
data class IOSNavigationEntry(
    val key: String,
    val route: String
)
