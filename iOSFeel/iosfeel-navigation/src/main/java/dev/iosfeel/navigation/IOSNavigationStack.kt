package dev.iosfeel.navigation

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import dev.iosfeel.gesture.IOSGestureAxisDirection
import dev.iosfeel.gesture.IOSGestureDecision
import dev.iosfeel.gesture.IOSGesturePhase
import dev.iosfeel.gesture.IOSGestureThresholds
import dev.iosfeel.gesture.decideDirectionalGestureCompletion
import dev.iosfeel.gesture.iosEdgeSwipe
import dev.iosfeel.gesture.rememberIOSGestureState
import dev.iosfeel.haptics.IOSHapticEvent
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun IOSNavigationStack(
    state: IOSNavigationState,
    modifier: Modifier = Modifier,
    backTransition: IOSBackTransitionState = remember { IOSBackTransitionState() },
    pushTransition: IOSPushTransitionState = remember { IOSPushTransitionState() },
    edgeWidthPx: Float = 220f,
    swipeBackEnabled: Boolean = true,
    content: @Composable (IOSNavigationEntry) -> Unit
) {
    val savedStateHolder = rememberSaveableStateHolder()
    val scope = rememberCoroutineScope()
    var widthPx by remember { mutableFloatStateOf(0f) }

    val backProgress = backTransition.currentProgress
    val pushProgress = pushTransition.progress.value
    val isPushing = pushTransition.isPushing

    val backTransforms = calculateIOSBackTransform(backProgress)
    val pushTransforms = calculateIOSPushTransform(pushProgress)

    var previousEntriesSize by remember { mutableStateOf(state.size) }
    var previousCurrentKey by remember { mutableStateOf(state.current.key) }

    LaunchedEffect(state.current.key, state.size) {
        if (state.size > previousEntriesSize && state.current.key != previousCurrentKey) {
            pushTransition.prepare()
            pushTransition.animate()
        }
        previousEntriesSize = state.size
        previousCurrentKey = state.current.key
    }

    val gesture = rememberIOSGestureState()
    val haptics = rememberIOSHaptics()

    val controller = remember(backTransition) {
        IOSInteractiveBackController(backTransition)
    }

    val thresholds = IOSGestureThresholds(
        progressThreshold = 0.38f,
        velocityThresholdPxPerSecond = 800f
    )

    val liveDecision = controller.decide(
        velocityPxPerSecond = gesture.velocityX,
        distancePx = if (widthPx > 0f) widthPx else 1080f,
        thresholds = thresholds
    )

    var previousDecision by remember { mutableStateOf(IOSGestureDecision.Cancel) }

    // Haptic feedback on threshold crossing during interactive drag
    LaunchedEffect(liveDecision, gesture.phase) {
        if (gesture.phase == IOSGesturePhase.Changed || gesture.phase == IOSGesturePhase.Began) {
            if (liveDecision != previousDecision) {
                when (liveDecision) {
                    IOSGestureDecision.Complete ->
                        haptics.perform(IOSHapticEvent.ThresholdActivated)
                    IOSGestureDecision.Cancel ->
                        haptics.perform(IOSHapticEvent.ThresholdDeactivated)
                }
                previousDecision = liveDecision
            }
        } else if (gesture.phase == IOSGesturePhase.Idle || gesture.phase == IOSGesturePhase.Ended || gesture.phase == IOSGesturePhase.Cancelled) {
            previousDecision = IOSGestureDecision.Cancel
        }
    }

    // Android 14+ Predictive Back Handler support
    PredictiveBackHandler(
        enabled = state.canGoBack && swipeBackEnabled
    ) { backEvents ->
        try {
            controller.begin()
            backEvents.collect { backEvent ->
                backTransition.updateInteractive(
                    progressValue = backEvent.progress,
                    progressVelocity = 0f
                )
            }
            backTransition.complete(initialVelocity = 0f)
            state.pop()
            backTransition.reset()
        } catch (cancellation: CancellationException) {
            backTransition.cancel(initialVelocity = backTransition.velocity)
        }
    }

    val edgeModifier = if (swipeBackEnabled) {
        Modifier.iosEdgeSwipe(
            state = gesture,
            edgeWidthPx = edgeWidthPx,
            progressDistancePx = if (widthPx > 0f) widthPx else 1080f,
            onStarted = {
                if (state.canGoBack) {
                    controller.begin()
                }
            },
            onChanged = { currentGesture ->
                if (state.canGoBack) {
                    controller.update(
                        gestureProgress = currentGesture.progress,
                        velocityPxPerSecond = currentGesture.velocityX,
                        distancePx = if (widthPx > 0f) widthPx else 1080f
                    )
                }
            },
            onEnded = { currentGesture ->
                if (state.canGoBack) {
                    val dist = if (widthPx > 0f) widthPx else 1080f
                    val finalVelocity = currentGesture.velocityX
                    val currentProgress = backTransition.currentProgress

                    // Authentic iOS back gesture completion:
                    // 1. Swiped past 20% of screen width without strong backward flick
                    // 2. OR flicked rightward with velocity >= 250 px/sec
                    val isFlickRight = finalVelocity >= 250f
                    val isDraggedPastThreshold = currentProgress >= 0.20f && finalVelocity >= -150f
                    val shouldComplete = isFlickRight || isDraggedPastThreshold

                    val normalizedVelocity = normalizeGestureVelocity(
                        velocityPxPerSecond = finalVelocity,
                        distancePx = dist
                    ).coerceIn(-8f, 8f)

                    scope.launch {
                        if (shouldComplete) {
                            backTransition.complete(initialVelocity = normalizedVelocity)
                            state.pop()
                            backTransition.reset()
                        } else {
                            backTransition.cancel(initialVelocity = normalizedVelocity)
                        }
                    }
                }
            },
            onCancelled = {
                if (state.canGoBack) {
                    scope.launch {
                        backTransition.cancel(initialVelocity = 0f)
                    }
                }
            }
        )
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                widthPx = it.width.toFloat()
            }
            .then(edgeModifier)
    ) {
        val previous = state.previous

        // 1. Previous screen underneath with parallax reveal
        if (previous != null) {
            val prevTranslation = if (isPushing) {
                widthPx * pushTransforms.previousTranslationFraction
            } else {
                widthPx * backTransforms.previousTranslationFraction
            }

            IOSNavigationScreen(
                entry = previous,
                stateHolder = savedStateHolder,
                translationX = prevTranslation,
                content = content
            )

            // Dark dimming scrim over previous screen that fades out
            val scrimAlpha = if (isPushing) {
                (1f - pushProgress) * 0.3f
            } else {
                backTransforms.shadowAlpha * 0.3f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = scrimAlpha }
                    .background(Color.Black)
            )
        }

        // 2. Current active screen on top
        val currentTranslation = if (isPushing) {
            widthPx * pushTransforms.currentTranslationFraction
        } else {
            widthPx * backTransforms.currentTranslationFraction
        }

        IOSNavigationScreen(
            entry = state.current,
            stateHolder = savedStateHolder,
            translationX = currentTranslation,
            content = content
        )

        // Left-edge shadow separation
        val showShadow = (previous != null) && (backProgress > 0.001f || isPushing)
        if (showShadow) {
            val shadowAlpha = if (isPushing) 1f - pushProgress else backTransforms.shadowAlpha
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = currentTranslation - 24.dp.toPx()
                        alpha = shadowAlpha
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f))
                        )
                    )
            )
        }
    }
}

@Composable
private fun IOSNavigationScreen(
    entry: IOSNavigationEntry,
    stateHolder: SaveableStateHolder,
    translationX: Float,
    content: @Composable (IOSNavigationEntry) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.translationX = translationX
            }
    ) {
        stateHolder.SaveableStateProvider(
            key = entry.key
        ) {
            content(entry)
        }
    }
}
