package dev.iosfeel.sheet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.iosfeel.gesture.IOSGestureConfig
import dev.iosfeel.gesture.IOSGestureDirection
import dev.iosfeel.gesture.iosGesture
import dev.iosfeel.gesture.rememberIOSGestureState
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IOSSheet(
    state: IOSSheetState,
    detents: List<IOSSheetDetent>,
    onDismissRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    config: IOSSheetConfig = IOSSheetConfig(),
    resolver: IOSSheetDetentResolver = IOSDefaultDetentResolver,
    backgroundContent: @Composable () -> Unit = {},
    content: @Composable IOSSheetScope.() -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = rememberIOSHaptics()
    val gestureState = rememberIOSGestureState()

    // Android Back button handler
    BackHandler(enabled = state.visible && config.dismissible) {
        scope.launch {
            val hPx = with(density) { 2400.dp.toPx() }
            state.dismiss(hPx, config.springSpec)
            onDismissRequest()
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val heightPx = with(density) { maxHeight.toPx() }
        val widthPx = with(density) { maxWidth.toPx() }

        val resolvedDetents = remember(heightPx, widthPx, detents, resolver) {
            resolveSheetDetents(
                containerHeightPx = heightPx,
                containerWidthPx = widthPx,
                detents = detents,
                resolver = resolver
            )
        }

        val minOffset = resolvedDetents.first().offsetPx
        val maxOffset = resolvedDetents.last().offsetPx

        // Update state resolved list and handle rotation/screen resize
        LaunchedEffect(resolvedDetents, heightPx) {
            state.resolved = resolvedDetents

            val target = findResolvedDetent(state.currentDetent, resolvedDetents)
                ?: resolvedDetents.first()

            if (!state.visible) {
                state.snapTo(heightPx)
            } else if (state.offset.value == 0f || state.phase == IOSSheetPhase.Idle) {
                state.snapTo(target.offsetPx)
            }
        }

        // Master expansion progress (0.0 at lowest, 1.0 at highest)
        val expansionProgress by remember(minOffset, maxOffset) {
            derivedStateOf {
                if (!state.visible) 0f
                else calculateSheetExpansionProgress(
                    offset = state.offset.value,
                    minOffset = minOffset,
                    maxOffset = maxOffset
                )
            }
        }

        // IME Keyboard detection and behavior
        val imeBottom = WindowInsets.ime.getBottom(density)
        val imeVisible = imeBottom > 0

        LaunchedEffect(imeVisible) {
            if (imeVisible && config.imeBehavior == IOSSheetImeBehavior.ExpandToLarge && state.visible) {
                state.animateTo(IOSSheetDetent.Large, springSpec = config.springSpec)
            }
        }

        // Haptic feedback on detent threshold crossing
        val currentNearestDetent by remember(resolvedDetents) {
            derivedStateOf {
                nearestDetent(state.offset.value, resolvedDetents).detent.id
            }
        }

        var lastHapticDetent by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(currentNearestDetent) {
            if (lastHapticDetent != null && lastHapticDetent != currentNearestDetent && state.phase == IOSSheetPhase.Dragging) {
                haptics.impact(IOSImpact.Light)
            }
            lastHapticDetent = currentNearestDetent
        }

        // Background layer with subtle scaling and corner transformation
        val bgScale = 1f - (1f - config.backgroundScaleMin) * expansionProgress
        val bgCornerRadius = (18f * expansionProgress).dp
        val scrimAlpha = config.scrimAlphaMax * expansionProgress

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                }
                .clip(RoundedCornerShape(bgCornerRadius))
        ) {
            backgroundContent()

            // Scrim overlay with tap to dismiss
            if (scrimAlpha > 0f && state.visible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = config.dismissOnScrimTap && config.dismissible
                        ) {
                            scope.launch {
                                state.dismiss(heightPx, config.springSpec)
                                onDismissRequest()
                            }
                        }
                )
            }
        }

        val nestedConnection = remember(state, resolvedDetents, heightPx, config) {
            IOSSheetNestedConnection(
                sheetState = state,
                scope = scope,
                detentsProvider = { resolvedDetents },
                containerHeightPxProvider = { heightPx },
                onDismissRequest = onDismissRequest,
                config = config
            )
        }

        var dragStartOffset by remember { mutableFloatStateOf(0f) }

        // Sheet Scope object
        val sheetScope = remember(state, expansionProgress) {
            IOSSheetScopeImpl(state = state, expansionProgress = expansionProgress)
        }

        // Sheet Layer
        if (state.visible || state.isSettling) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = state.offset.value
                    }
                    .nestedScroll(nestedConnection)
                    .semantics {
                        contentDescription = "Bottom Sheet"
                        stateDescription = state.currentDetent.id
                        customActions = listOf(
                            CustomAccessibilityAction("Expand sheet") {
                                scope.launch { state.expand(config.springSpec) }
                                true
                            },
                            CustomAccessibilityAction("Collapse sheet") {
                                scope.launch { state.collapse(config.springSpec) }
                                true
                            }
                        )
                        if (config.dismissible) {
                            dismiss {
                                scope.launch {
                                    state.dismiss(heightPx, config.springSpec)
                                    onDismissRequest()
                                }
                                true
                            }
                        }
                    }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(
                        topStart = config.cornerRadiusDp.dp,
                        topEnd = config.cornerRadiusDp.dp
                    ),
                    color = Color(0xFF1C1C1E)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (config.useImePadding) Modifier.imePadding() else Modifier)
                            .then(if (config.useImeNestedScroll) Modifier.imeNestedScroll() else Modifier)
                    ) {
                        // Grabber Header Bar
                        if (config.showGrabber) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .iosGesture(
                                        state = gestureState,
                                        config = IOSGestureConfig(
                                            direction = IOSGestureDirection.Vertical,
                                            progressDistancePx = heightPx
                                        ),
                                        onStarted = {
                                            dragStartOffset = state.offset.value
                                            scope.launch { state.beginDrag() }
                                        },
                                        onChanged = {
                                            scope.launch {
                                                val maxDragLimit = if (config.dismissible) heightPx else maxOffset
                                                val targetOffset = (dragStartOffset + gestureState.translationY)
                                                    .coerceIn(minOffset, maxDragLimit)
                                                state.snapTo(targetOffset)
                                            }
                                        },
                                        onEnded = {
                                            val target = chooseSheetTarget(
                                                currentOffset = state.offset.value,
                                                velocityY = gestureState.velocityY,
                                                detents = resolvedDetents,
                                                velocityThreshold = config.velocityThreshold,
                                                dismissible = config.dismissible,
                                                dismissVelocityThreshold = config.dismissVelocityThreshold
                                            )
                                            scope.launch {
                                                when (target) {
                                                    is IOSSheetTarget.Detent -> {
                                                        state.settleTo(
                                                            target = target.value,
                                                            initialVelocity = gestureState.velocityY,
                                                            springSpec = config.springSpec
                                                        )
                                                    }
                                                    is IOSSheetTarget.Dismiss -> {
                                                        state.dismiss(heightPx, config.springSpec)
                                                        onDismissRequest()
                                                    }
                                                }
                                            }
                                        },
                                        onCancelled = {
                                            val target = nearestDetent(state.offset.value, resolvedDetents)
                                            scope.launch {
                                                state.settleTo(
                                                    target = target,
                                                    initialVelocity = 0f,
                                                    springSpec = config.springSpec
                                                )
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Grabber pill (36x5dp)
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF8E8E93).copy(alpha = 0.6f))
                                )
                            }
                        }

                        // Sheet Body Content with Scope
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            sheetScope.content()
                        }
                    }
                }
            }
        }
    }
}
