package dev.iosfeel.sheet

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.nested.IOSSheetNestedScrollCoordinator
import kotlinx.coroutines.CoroutineScope

/**
 * Backward compatibility wrapper around [IOSSheetNestedScrollCoordinator].
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSSheetNestedConnection(
    private val sheetState: IOSSheetState,
    private val scope: CoroutineScope,
    private val detentsProvider: () -> List<Any> = { emptyList() },
    private val containerHeightPxProvider: () -> Float = { sheetState.containerHeightPx },
    private val onDismissRequest: () -> Unit = {},
    private val config: IOSSheetConfig = IOSSheetConfig()
) : NestedScrollConnection {

    private val coordinator = IOSSheetNestedScrollCoordinator(sheetState, scope)

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        return coordinator.onPreScroll(available, source)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        return coordinator.onPostScroll(consumed, available, source)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return coordinator.onPreFling(available)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        return coordinator.onPostFling(consumed, available)
    }
}
