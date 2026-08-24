package dev.iosfeel.navigation.back

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import dev.iosfeel.navigation.transition.IOSNavigationTransitionSource
import dev.iosfeel.navigation.transition.IOSNavigationTransitionState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Connects Android system-level Predictive Back gestures directly to the iOSFeel V2 navigation transition engine.
 */
@Composable
@ExperimentalIOSFeelV2Api
fun IOSPredictiveBackAdapter(
    transitionState: IOSNavigationTransitionState,
    enabled: Boolean = true,
    onPopComplete: () -> Unit
) {
    PredictiveBackHandler(enabled = enabled) { progressFlow ->
        IOSBackGestureCoordinator.startBackGesture(
            transition = transitionState,
            source = IOSNavigationTransitionSource.PredictiveBack
        )

        try {
            progressFlow.collect { backEvent ->
                transitionState.motion.dragTo(
                    value = backEvent.progress,
                    velocity = 0f
                )
            }

            // Gesture committed by user
            transitionState.animateTo(target = 1f)
            onPopComplete()
        } catch (e: CancellationException) {
            // Gesture cancelled by user
            transitionState.animateTo(target = 0f)
            throw e
        }
    }
}
