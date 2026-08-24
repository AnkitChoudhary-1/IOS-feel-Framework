package dev.iosfeel.sheet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.detent.IOSSheetDetent
import dev.iosfeel.sheet.nested.rememberIOSSheetNestedScrollCoordinator
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSSheet(
    state: IOSSheetState,
    detents: List<IOSSheetDetent> = state.detents,
    onDismissRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    config: IOSSheetConfig = IOSSheetConfig(),
    backgroundContent: @Composable () -> Unit = {},
    content: @Composable IOSSheetScope.() -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptics = rememberIOSHaptics()

    // Android Back button handler
    BackHandler(enabled = state.isVisible && config.dismissible) {
        scope.launch {
            state.dismiss()
            onDismissRequest()
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val heightPx = with(density) { maxHeight.toPx() }
        val widthPx = with(density) { maxWidth.toPx() }

        // Keep state informed of latest geometry
        LaunchedEffect(heightPx) {
            state.containerHeightPx = heightPx
            if (!state.isVisible && state.phase == IOSSheetPhase.Idle) {
                state.snapTo(IOSSheetDetent.Hidden)
            }
        }

        val expansionProgress = state.expansionProgress

        // Background scaling
        val bgScale = 1f - (1f - config.backgroundScaleMin) * expansionProgress
        val bgCornerRadius = (12f * expansionProgress).dp
        val scrimAlpha = config.scrimAlphaMax * expansionProgress

        // Background Layer
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
            if (scrimAlpha > 0.01f && (state.isVisible || state.isSettling)) {
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
                                state.dismiss()
                                onDismissRequest()
                            }
                        }
                )
            }
        }

        val nestedCoordinator = rememberIOSSheetNestedScrollCoordinator(sheetState = state)

        // Sheet Scope object
        val sheetScope = remember(state, expansionProgress) {
            IOSSheetScopeImpl(state = state, expansionProgress = expansionProgress)
        }

        // Sheet Layer
        if (state.isVisible || state.isSettling) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = state.offset
                    }
                    .nestedScroll(nestedCoordinator)
                    .semantics {
                        contentDescription = "Bottom Sheet"
                        stateDescription = state.currentDetent.id
                        customActions = listOf(
                            CustomAccessibilityAction("Expand sheet") {
                                scope.launch { state.expand() }
                                true
                            },
                            CustomAccessibilityAction("Collapse sheet") {
                                scope.launch { state.collapse() }
                                true
                            }
                        )
                        if (config.dismissible) {
                            dismiss {
                                scope.launch {
                                    state.dismiss()
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
                            val velocityTracker = remember { VelocityTracker() }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .pointerInput(heightPx, config) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                velocityTracker.resetTracking()
                                                state.beginDrag()
                                            },
                                            onDragEnd = {
                                                val velocityY = velocityTracker.calculateVelocity().y
                                                scope.launch {
                                                    state.release(velocityPxPerSec = velocityY)
                                                    if (state.currentDetent == IOSSheetDetent.Hidden) {
                                                        onDismissRequest()
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                scope.launch {
                                                    state.release(velocityPxPerSec = 0f)
                                                }
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                                val targetOffset = state.offset + dragAmount
                                                state.dragTo(targetOffset)
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Rounded Grabber Pill
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 5.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(100)
                                        )
                                )
                            }
                        }

                        // Sheet Content
                        sheetScope.content()
                    }
                }
            }
        }
    }
}
