package dev.iosfeel.sheet.back

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.IOSSheetPhase
import dev.iosfeel.sheet.IOSSheetState
import dev.iosfeel.sheet.detent.IOSSheetDetent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Connects Android Predictive Back gestures to [IOSSheetState] for interactive dismissals.
 */
@Composable
@ExperimentalIOSFeelV2Api
fun IOSSheetPredictiveBack(
    sheetState: IOSSheetState,
    enabled: Boolean = sheetState.isVisible,
    onDismissed: () -> Unit = {}
) {
    PredictiveBackHandler(enabled = enabled) { progressFlow ->
        val initialOffset = sheetState.offset
        val hiddenOffset = sheetState.containerHeightPx
        val totalSpan = hiddenOffset - initialOffset

        sheetState.acquireByUser()

        try {
            progressFlow.collect { backEvent ->
                val progress = backEvent.progress
                val currentOffset = initialOffset + (totalSpan * progress)
                sheetState.dragTo(currentOffset, 0f)
            }

            // User committed gesture -> dismiss
            sheetState.dismiss()
            onDismissed()
        } catch (e: CancellationException) {
            // User cancelled gesture -> spring back to current detent
            sheetState.animateTo(sheetState.currentDetent)
            throw e
        }
    }
}
