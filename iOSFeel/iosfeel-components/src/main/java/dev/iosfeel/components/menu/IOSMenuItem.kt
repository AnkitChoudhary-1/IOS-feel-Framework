package dev.iosfeel.components.menu

import androidx.compose.runtime.Immutable
import dev.iosfeel.core.tokens.IOSActionRole

typealias IOSMenuRole = IOSActionRole

@Immutable
data class IOSMenuItem(
    val label: String,
    val icon: String? = null,
    val role: IOSActionRole = IOSActionRole.Normal,
    val onClick: () -> Unit
)
