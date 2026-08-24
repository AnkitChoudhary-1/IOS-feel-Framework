package dev.iosfeel.components.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import dev.iosfeel.interaction.IOSInteractionPhase
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings
import kotlinx.coroutines.launch

/**
 * Universal state representing interactive press lifecycle and physics.
 *
 * Drives scale, opacity, and content elevation for iOS controls.
 *
 * @property progress Normalized press progress from 0f (unpressed) to 1f (fully compressed).
 * @property phase Current interaction lifecycle phase.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSPressSurfaceState(
    val pressSpring: IOSSpringSpec = IOSSprings.Press,
    val releaseSpring: IOSSpringSpec = IOSSprings.Press
) {
    internal val animatable = Animatable(0f)

    val progress: Float
        get() = animatable.value

    var phase: IOSInteractionPhase by mutableStateOf(IOSInteractionPhase.Idle)
        internal set

    val isPressed: Boolean
        get() = phase == IOSInteractionPhase.Pressed

    suspend fun press() {
        phase = IOSInteractionPhase.Pressed
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = SpringSpec(
                dampingRatio = pressSpring.dampingRatio,
                stiffness = pressSpring.stiffness
            )
        )
    }

    suspend fun release() {
        phase = IOSInteractionPhase.Settling
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = SpringSpec(
                dampingRatio = releaseSpring.dampingRatio,
                stiffness = releaseSpring.stiffness
            )
        )
        phase = IOSInteractionPhase.Idle
    }

    suspend fun cancel() {
        phase = IOSInteractionPhase.Cancelled
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = SpringSpec(
                dampingRatio = releaseSpring.dampingRatio,
                stiffness = releaseSpring.stiffness * 1.2f
            )
        )
        phase = IOSInteractionPhase.Idle
    }
}

/**
 * Remembers an [IOSPressSurfaceState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSPressSurfaceState(
    pressSpring: IOSSpringSpec = IOSSprings.Press,
    releaseSpring: IOSSpringSpec = IOSSprings.Press
): IOSPressSurfaceState {
    return remember(pressSpring, releaseSpring) {
        IOSPressSurfaceState(pressSpring, releaseSpring)
    }
}

/**
 * Applies physical iOS press interaction to any composable surface.
 *
 * When touched, smoothly compresses the surface down to [pressedScale] and elevates opacity.
 * If user begins scrolling or arena cancels the touch, it gracefully releases without jumping.
 *
 * @param state State tracking press progress and phase.
 * @param enabled Whether press gestures are active.
 * @param pressedScale Minimum scale reached when fully pressed (e.g. 0.96f for buttons, 0.995f for list rows).
 * @param pressedAlpha Minimum alpha multiplier when pressed (1f = no change).
 * @param onClick Optional click callback invoked on clean release.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
fun Modifier.iosPressSurface(
    state: IOSPressSurfaceState,
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    pressedAlpha: Float = 1.0f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val scope = rememberCoroutineScope()

    this
        .graphicsLayer {
            val p = state.progress
            val s = 1f - (1f - pressedScale) * p
            val a = 1f - (1f - pressedAlpha) * p
            scaleX = s
            scaleY = s
            alpha = a
        }
        .then(
            if (enabled) {
                Modifier.pointerInput(enabled, onClick) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        scope.launch { state.press() }

                        val upOrCancel = waitForUpOrCancellation()
                        if (upOrCancel != null && !upOrCancel.isConsumed) {
                            upOrCancel.consume()
                            scope.launch {
                                state.release()
                                onClick?.invoke()
                            }
                        } else {
                            scope.launch { state.cancel() }
                        }
                    }
                }
            } else Modifier
        )
}
