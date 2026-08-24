package dev.iosfeel.components.floatingbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import dev.iosfeel.components.badge.IOSBadge
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.core.tokens.IOSMotionTokens
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

@Composable
fun <T> IOSFloatingTabBar(
    items: List<IOSFloatingTabItem<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    onTabReselected: ((T) -> Unit)? = null,
    accessory: (@Composable () -> Unit)? = null,
    state: IOSFloatingBarState = rememberIOSFloatingBarState(),
    scrubConfig: IOSFloatingTabScrubConfig = IOSFloatingTabScrubConfig(),
    backdrop: IOSBackdropState? = null,
    activeColor: Color = Color(0xFF007AFF),
    inactiveColor: Color = Color(0xFF8E8E93),
    horizontalPadding: Dp = IOSFloatingBarDefaults.HorizontalPadding,
    bottomPadding: Dp = IOSFloatingBarDefaults.BottomPadding,
    hapticsEnabled: Boolean = true
) {
    val density = LocalDensity.current
    val haptics = rememberIOSHaptics()
    val progress = state.progress.coerceIn(0f, 1f)

    val currentHeight = lerp(IOSFloatingBarDefaults.Height, IOSFloatingBarDefaults.CompactHeight, progress).coerceAtLeast(0.dp)
    val horizontalMargin = lerp(horizontalPadding, horizontalPadding + 16.dp, progress).coerceAtLeast(0.dp)

    val selectedIndex = remember(items, selected) {
        val idx = items.indexOfFirst { it.value == selected }
        if (idx >= 0) idx else 0
    }

    val scrubState = rememberIOSFloatingTabScrubState(
        initialSelectedIndex = selectedIndex,
        config = scrubConfig
    )

    LaunchedEffect(selectedIndex) {
        scrubState.syncSelectedIndex(selectedIndex)
    }

    val barScale = lerp(1f, scrubConfig.barPressedScale, scrubState.barCompression)
    val barTranslationY = with(density) { (scrubState.barCompression * scrubConfig.barReactionDistance.toPx()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = bottomPadding)
            .padding(horizontal = horizontalMargin),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Accessory slot (e.g. Floating MiniPlayer)
            if (accessory != null) {
                accessory()
                Spacer(modifier = Modifier.height(8.dp))
            }

            IOSFloatingBar(
                backdrop = backdrop,
                materialStyle = IOSFloatingMaterialStyle.Regular,
                shape = IOSFloatingShapes.Bar,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = barScale
                        scaleY = barScale
                        translationY = barTranslationY
                    }
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentHeight)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .selectableGroup()
                    .pointerInput(scrubConfig, items, selectedIndex) {
                        if (!scrubConfig.enabled) return@pointerInput
                        val cancelDistPx = scrubConfig.verticalCancelDistance.toPx()

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downX = down.position.x
                            val downY = down.position.y

                            // Determine if touch is on the active tab
                            var touchIndex = -1
                            for (i in scrubState.tabCenters.indices) {
                                val center = scrubState.tabCenters[i]
                                val width = scrubState.tabWidths.getOrElse(i) { 60f }
                                if (downX >= center - width / 2f && downX <= center + width / 2f) {
                                    touchIndex = i
                                    break
                                }
                            }

                            if (touchIndex == selectedIndex) {
                                scrubState.onPressDown(selectedIndex)

                                // Wait for long-press hold duration
                                val held = withTimeoutOrNull(scrubConfig.longPressDurationMillis) {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (!change.pressed) {
                                            return@withTimeoutOrNull false
                                        }
                                        // If moved too far before hold, don't trigger scrub
                                        if (abs(change.position.x - downX) > 20f || abs(change.position.y - downY) > 20f) {
                                            return@withTimeoutOrNull false
                                        }
                                    }
                                    true
                                }

                                if (held == null) {
                                    // Long-press threshold reached while holding down
                                    scrubState.onHoldTriggered(if (hapticsEnabled) haptics else null)

                                    // Enter continuous scrub drag loop
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                        if (!change.pressed) {
                                            scrubState.onRelease { targetIndex ->
                                                if (targetIndex in items.indices) {
                                                    onSelected(items[targetIndex].value)
                                                }
                                            }
                                            break
                                        }

                                        change.consume()
                                        val currentX = change.position.x
                                        val verticalDisplacement = change.position.y - downY

                                        scrubState.onDrag(
                                            currentX = currentX,
                                            verticalDisplacementPx = verticalDisplacement,
                                            maxVerticalCancelPx = cancelDistPx,
                                            haptics = if (hapticsEnabled) haptics else null
                                        )
                                    }
                                } else {
                                    // Released before long-press threshold -> cancel scrub state and allow regular tap
                                    scrubState.onCancel()
                                }
                            }
                        }
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = item.value == selected
                    val isEffectiveSelected = if (scrubState.phase == IOSFloatingTabInteractionPhase.Idle) {
                        isSelected
                    } else {
                        scrubState.hoveredIndex == index
                    }
                    val interactionSource = remember { MutableInteractionSource() }

                    val scaleAnim = remember { Animatable(1f) }
                    LaunchedEffect(isSelected) {
                        if (isSelected) {
                            scaleAnim.animateTo(
                                targetValue = 1.12f,
                                animationSpec = spring(
                                    stiffness = IOSMotionTokens.TabBarStiffness,
                                    dampingRatio = IOSMotionTokens.TabBarDampingRatio
                                )
                            )
                            scaleAnim.animateTo(
                                targetValue = 1.0f,
                                animationSpec = spring(
                                    stiffness = IOSMotionTokens.TabBarStiffness,
                                    dampingRatio = IOSMotionTokens.TabBarDampingRatio
                                )
                            )
                        }
                    }

                    val labelAlpha = (1f - progress * 2.5f).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                val pos = coordinates.positionInParent()
                                val width = coordinates.size.width.toFloat()
                                scrubState.updateTabBounds(
                                    index = index,
                                    centerPx = pos.x + width / 2f,
                                    widthPx = width
                                )
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isEffectiveSelected) {
                                    activeColor.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .semantics {
                                role = Role.Tab
                                this.selected = isSelected
                            }
                            .iosPressEffect(
                                interactionSource = interactionSource,
                                config = IOSPressConfig(pressedScale = 0.93f)
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (isSelected) {
                                    onTabReselected?.invoke(item.value)
                                } else {
                                    if (hapticsEnabled) {
                                        haptics.selection()
                                    }
                                    onSelected(item.value)
                                }
                            }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.graphicsLayer {
                                scaleX = scaleAnim.value
                                scaleY = scaleAnim.value
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                item.icon(isEffectiveSelected)

                                if (item.badgeCount != null && item.badgeCount > 0) {
                                    IOSBadge(
                                        count = item.badgeCount,
                                        modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
                                    )
                                } else if (item.showBadgeDot) {
                                    IOSBadge(
                                        count = null,
                                        modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                                    )
                                }
                            }

                            if (item.label != null && labelAlpha > 0.05f) {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isEffectiveSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isEffectiveSelected) activeColor else inactiveColor,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .graphicsLayer { alpha = labelAlpha }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

