package dev.iosfeel.sheet

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class IOSSheetNestedConnection(
    private val sheetState: IOSSheetState,
    private val scope: CoroutineScope,
    private val detentsProvider: () -> List<IOSResolvedDetent>,
    private val containerHeightPxProvider: () -> Float,
    private val onDismissRequest: () -> Unit = {},
    private val config: IOSSheetConfig = IOSSheetConfig()
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val deltaY = available.y

        // Scrolling upward (finger moves up, content expands)
        if (deltaY >= 0f) {
            return Offset.Zero
        }

        val detents = detentsProvider()
        if (detents.isEmpty()) return Offset.Zero

        val topDetentOffset = detents.first().offsetPx

        // If sheet is already at top, let child scroll freely
        if (sheetState.offset.value <= topDetentOffset) {
            return Offset.Zero
        }

        val availableExpansion = sheetState.offset.value - topDetentOffset
        val requested = -deltaY
        val consumed = minOf(requested, availableExpansion)

        scope.launch {
            val newOffset = sheetState.offset.value - consumed
            sheetState.dragTo(newOffset)
        }

        return Offset(x = 0f, y = -consumed)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val deltaY = available.y

        // Pulling downward (finger moves down, collapsing sheet)
        if (deltaY <= 0f) {
            return Offset.Zero
        }

        val detents = detentsProvider()
        if (detents.isEmpty()) return Offset.Zero

        val containerHeight = containerHeightPxProvider()
        val bottomLimit = if (config.dismissible) containerHeight else detents.last().offsetPx
        val availableCollapse = (bottomLimit - sheetState.offset.value).coerceAtLeast(0f)
        val consumedCollapse = minOf(deltaY, availableCollapse)

        if (consumedCollapse > 0f) {
            scope.launch {
                val newOffset = sheetState.offset.value + consumedCollapse
                sheetState.dragTo(newOffset)
            }
            return Offset(x = 0f, y = consumedCollapse)
        }

        return Offset.Zero
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity {
        val detents = detentsProvider()
        if (detents.isEmpty()) return Velocity.Zero

        val containerHeight = containerHeightPxProvider()
        val target = chooseSheetTarget(
            currentOffset = sheetState.offset.value,
            velocityY = available.y,
            detents = detents,
            containerHeightPx = containerHeight,
            velocityThreshold = config.velocityThreshold,
            dismissible = config.dismissible,
            dismissVelocityThreshold = config.dismissVelocityThreshold
        )

        when (target) {
            is IOSSheetTarget.Detent -> {
                sheetState.settleTo(
                    target = target.value,
                    initialVelocity = available.y,
                    springSpec = config.springSpec
                )
            }
            is IOSSheetTarget.Dismiss -> {
                sheetState.dismiss(
                    containerHeightPx = containerHeight,
                    springSpec = config.springSpec
                )
                onDismissRequest()
            }
        }

        return Velocity.Zero
    }
}
