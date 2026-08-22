package dev.iosfeel.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun IOSScrollSurface(
    modifier: Modifier = Modifier,
    state: IOSScrollState = rememberIOSScrollState(),
    config: IOSScrollConfig = IOSScrollConfig(),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val overscrollAnim = remember { Animatable(0f) }

    val draggableState = rememberDraggableState { delta ->
        val result = consumeIOSScrollDelta(
            state = state,
            delta = delta,
            config = config
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                // Assumes viewport size is captured
            }
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStarted = {
                    scope.launch {
                        overscrollAnim.stop()
                        state.phase = IOSScrollPhase.Dragging
                    }
                },
                onDragStopped = { velocity ->
                    state.velocity = velocity

                    if (state.overscroll != 0f) {
                        scope.launch {
                            state.phase = IOSScrollPhase.SpringingBack
                            overscrollAnim.snapTo(state.overscroll)
                            overscrollAnim.animateTo(
                                targetValue = 0f,
                                initialVelocity = velocity,
                                animationSpec = spring(
                                    stiffness = config.springStiffness,
                                    dampingRatio = config.springDampingRatio
                                )
                            ) {
                                state.overscroll = this.value
                            }
                            state.overscroll = 0f
                            state.phase = IOSScrollPhase.Idle
                        }
                    } else {
                        state.phase = IOSScrollPhase.Idle
                    }
                }
            )
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = -state.position + state.overscroll
                }
                .onSizeChanged { contentSize ->
                    // Set maxScroll based on content height
                    val viewportHeight = 800f // Fallback estimate; updated dynamically
                    state.maxScroll = max(0f, contentSize.height.toFloat() - viewportHeight)
                }
        ) {
            content()
        }
    }
}
