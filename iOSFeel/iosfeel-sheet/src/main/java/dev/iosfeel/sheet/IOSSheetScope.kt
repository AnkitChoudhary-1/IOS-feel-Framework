package dev.iosfeel.sheet

import androidx.compose.runtime.Stable

@Stable
interface IOSSheetScope {
    val state: IOSSheetState
    val expansionProgress: Float
}

internal class IOSSheetScopeImpl(
    override val state: IOSSheetState,
    override val expansionProgress: Float
) : IOSSheetScope
