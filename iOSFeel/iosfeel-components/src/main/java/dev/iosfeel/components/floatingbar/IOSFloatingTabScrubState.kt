package dev.iosfeel.components.floatingbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.iosfeel.haptics.IOSHaptics
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Manages the physical state, continuous coordinates, and coupled action-reaction
 * animations for the iOS floating tab bar scrub interaction.
 */
@Stable
class IOSFloatingTabScrubState(
    initialSelectedIndex: Int = 0,
    val config: IOSFloatingTabScrubConfig = IOSFloatingTabScrubConfig(),
    private val scope: CoroutineScope
) {
    var selectedIndex by mutableIntStateOf(initialSelectedIndex)
        internal set

    var hoveredIndex by mutableIntStateOf(initialSelectedIndex)
        internal set

    var phase by mutableStateOf(IOSFloatingTabInteractionPhase.Idle)
        internal set

    /** Continuous horizontal physical pixel coordinate of the selector center. */
    var dragX by mutableFloatStateOf(0f)
        internal set

    /** 0f (resting inside bar) to 1f (fully lifted and grabbed). */
    val liftProgressAnim = Animatable(0f)
    val liftProgress: Float get() = liftProgressAnim.value

    /** 0f (normal) to 1f (bar compressed downward / reaction scale). */
    val barCompressionAnim = Animatable(0f)
    val barCompression: Float get() = barCompressionAnim.value

    /** Selector scale animation during press and hold. */
    val selectorScaleAnim = Animatable(1f)
    val selectorScale: Float get() = selectorScaleAnim.value

    internal val tabCenters = mutableStateListOf<Float>()
    internal val tabWidths = mutableStateListOf<Float>()

    private var animationJob: Job? = null

    fun updateTabBounds(index: Int, centerPx: Float, widthPx: Float) {
        while (tabCenters.size <= index) {
            tabCenters.add(0f)
            tabWidths.add(0f)
        }
        tabCenters[index] = centerPx
        tabWidths[index] = widthPx

        if (phase == IOSFloatingTabInteractionPhase.Idle && index == selectedIndex) {
            dragX = centerPx
        }
    }

    fun syncSelectedIndex(index: Int) {
        selectedIndex = index
        if (phase == IOSFloatingTabInteractionPhase.Idle) {
            hoveredIndex = index
            if (index in tabCenters.indices) {
                dragX = tabCenters[index]
            }
        }
    }

    /**
     * Initial touch down on a tab.
     */
    fun onPressDown(index: Int) {
        if (!config.enabled) return
        animationJob?.cancel()
        phase = IOSFloatingTabInteractionPhase.Pressing
        if (index in tabCenters.indices) {
            dragX = tabCenters[index]
        }
        scope.launch {
            selectorScaleAnim.animateTo(
                targetValue = config.pressedScale,
                animationSpec = spring(
                    stiffness = config.coupledSpringSpec.primary.stiffness,
                    dampingRatio = config.coupledSpringSpec.primary.dampingRatio
                )
            )
        }
    }

    /**
     * Triggered when the long-press hold threshold is reached on the active tab.
     * The selector capsule springs upward into a lifted state, and the navbar pill
     * undergoes a subtle Newtonian downward compression.
     */
    fun onHoldTriggered(haptics: IOSHaptics? = null) {
        if (!config.enabled) return
        phase = IOSFloatingTabInteractionPhase.Held
        haptics?.selection()

        animationJob?.cancel()
        animationJob = scope.launch {
            // Primary body: selector lifts and scales up
            launch {
                liftProgressAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.primary.stiffness,
                        dampingRatio = config.coupledSpringSpec.primary.dampingRatio
                    )
                )
            }
            launch {
                selectorScaleAnim.animateTo(
                    targetValue = config.heldScale,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.primary.stiffness,
                        dampingRatio = config.coupledSpringSpec.primary.dampingRatio
                    )
                )
            }
            // Reactive body: navbar pill compresses slightly down
            launch {
                barCompressionAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.reaction.stiffness,
                        dampingRatio = config.coupledSpringSpec.reaction.dampingRatio
                    )
                )
            }
        }
    }

    /**
     * Continuous horizontal dragging during scrub mode.
     */
    fun onDrag(
        currentX: Float,
        verticalDisplacementPx: Float,
        maxVerticalCancelPx: Float,
        haptics: IOSHaptics? = null
    ) {
        if (phase != IOSFloatingTabInteractionPhase.Held && phase != IOSFloatingTabInteractionPhase.Scrubbing) {
            return
        }
        phase = IOSFloatingTabInteractionPhase.Scrubbing

        // Check if user dragged too far vertically to cancel
        if (abs(verticalDisplacementPx) > maxVerticalCancelPx) {
            onCancel()
            return
        }

        // Clamp dragX within valid tab bounds
        if (tabCenters.isNotEmpty()) {
            val minX = tabCenters.first() - (tabWidths.firstOrNull() ?: 0f) / 4f
            val maxX = tabCenters.last() + (tabWidths.lastOrNull() ?: 0f) / 4f
            dragX = currentX.coerceIn(minX, maxX)

            // Compute the closest hovered tab index
            var closestIndex = hoveredIndex
            var minDistance = Float.MAX_VALUE
            for (i in tabCenters.indices) {
                val dist = abs(dragX - tabCenters[i])
                if (dist < minDistance) {
                    minDistance = dist
                    closestIndex = i
                }
            }

            if (closestIndex != hoveredIndex) {
                hoveredIndex = closestIndex
                if (config.hapticDetents) {
                    haptics?.selection()
                }
            }
        }
    }

    /**
     * User released finger. Springs lifted selector down to target tab,
     * triggers destination navigation, and triggers coupled navbar rebound.
     */
    fun onRelease(
        onSelect: (Int) -> Unit
    ) {
        if (phase == IOSFloatingTabInteractionPhase.Idle) return

        val targetIndex = hoveredIndex
        selectedIndex = targetIndex
        phase = IOSFloatingTabInteractionPhase.Settling
        onSelect(targetIndex)

        val targetX = if (targetIndex in tabCenters.indices) tabCenters[targetIndex] else dragX

        animationJob?.cancel()
        animationJob = scope.launch {
            launch {
                liftProgressAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.primary.stiffness,
                        dampingRatio = config.coupledSpringSpec.primary.dampingRatio
                    )
                )
            }
            launch {
                selectorScaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.primary.stiffness,
                        dampingRatio = config.coupledSpringSpec.primary.dampingRatio
                    )
                )
            }
            launch {
                barCompressionAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.reaction.stiffness,
                        dampingRatio = config.coupledSpringSpec.reaction.dampingRatio
                    )
                )
            }

            dragX = targetX
            phase = IOSFloatingTabInteractionPhase.Idle
        }
    }

    /**
     * Cancels scrubbing, smoothly returning selector to the original selected tab.
     */
    fun onCancel() {
        if (phase == IOSFloatingTabInteractionPhase.Idle) return
        phase = IOSFloatingTabInteractionPhase.Settling
        hoveredIndex = selectedIndex

        val originX = if (selectedIndex in tabCenters.indices) tabCenters[selectedIndex] else dragX

        animationJob?.cancel()
        animationJob = scope.launch {
            launch {
                liftProgressAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.primary.stiffness,
                        dampingRatio = config.coupledSpringSpec.primary.dampingRatio
                    )
                )
            }
            launch {
                selectorScaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.primary.stiffness,
                        dampingRatio = config.coupledSpringSpec.primary.dampingRatio
                    )
                )
            }
            launch {
                barCompressionAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = config.coupledSpringSpec.reaction.stiffness,
                        dampingRatio = config.coupledSpringSpec.reaction.dampingRatio
                    )
                )
            }

            dragX = originX
            phase = IOSFloatingTabInteractionPhase.Idle
        }
    }
}

/**
 * Remember an [IOSFloatingTabScrubState] instance across recompositions.
 */
@Composable
fun rememberIOSFloatingTabScrubState(
    initialSelectedIndex: Int = 0,
    config: IOSFloatingTabScrubConfig = IOSFloatingTabScrubConfig()
): IOSFloatingTabScrubState {
    val scope = rememberCoroutineScope()
    return remember(config) {
        IOSFloatingTabScrubState(
            initialSelectedIndex = initialSelectedIndex,
            config = config,
            scope = scope
        )
    }
}
