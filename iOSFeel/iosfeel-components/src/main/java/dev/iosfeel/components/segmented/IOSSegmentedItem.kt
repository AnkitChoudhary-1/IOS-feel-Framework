package dev.iosfeel.components.segmented

import androidx.compose.runtime.Immutable

@Immutable
data class IOSSegmentedItem<T>(
    val value: T,
    val label: String
)
