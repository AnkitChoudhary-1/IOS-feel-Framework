package dev.iosfeel.sheet

import dev.iosfeel.sheet.detent.IOSSheetDetent
import kotlin.math.abs

data class IOSResolvedDetent(
    val detent: IOSSheetDetent,
    val offsetPx: Float
)

sealed interface IOSSheetTarget {
    data class Detent(val value: IOSResolvedDetent) : IOSSheetTarget
    data object Dismiss : IOSSheetTarget
}

fun nearestDetent(
    currentOffset: Float,
    detents: List<IOSResolvedDetent>
): IOSResolvedDetent {
    require(detents.isNotEmpty()) { "detents list must not be empty" }
    return detents.minBy { abs(it.offsetPx - currentOffset) }
}

fun chooseSheetTarget(
    currentOffset: Float,
    velocityY: Float,
    detents: List<IOSResolvedDetent>,
    containerHeightPx: Float,
    velocityThreshold: Float = 600f,
    dismissible: Boolean = true,
    dismissVelocityThreshold: Float = 900f
): IOSSheetTarget {
    require(detents.isNotEmpty()) { "detents list must not be empty" }

    val lowestDetent = detents.last()
    val dismissThreshold = lowestDetent.offsetPx + (containerHeightPx - lowestDetent.offsetPx) * 0.35f

    // 1. Strong downward flick or dragged past dismiss threshold
    if (dismissible) {
        if (currentOffset >= dismissThreshold) {
            return IOSSheetTarget.Dismiss
        }
        if (currentOffset >= lowestDetent.offsetPx && velocityY >= dismissVelocityThreshold) {
            return IOSSheetTarget.Dismiss
        }
    }

    // 2. Velocity-based target selection
    if (abs(velocityY) >= velocityThreshold) {
        val nearestIndex = detents.indices.minBy { abs(detents[it].offsetPx - currentOffset) }
        return if (velocityY > 0f) {
            if (dismissible && nearestIndex == detents.lastIndex) {
                IOSSheetTarget.Dismiss
            } else {
                val nextIndex = (nearestIndex + 1).coerceAtMost(detents.lastIndex)
                IOSSheetTarget.Detent(detents[nextIndex])
            }
        } else {
            val nextIndex = (nearestIndex - 1).coerceAtLeast(0)
            IOSSheetTarget.Detent(detents[nextIndex])
        }
    }

    // 3. Positional nearest detent
    val nearest = detents.minBy { abs(it.offsetPx - currentOffset) }
    return IOSSheetTarget.Detent(nearest)
}

fun calculateSheetExpansionProgress(
    offset: Float,
    minOffset: Float,
    maxOffset: Float
): Float {
    if (maxOffset <= minOffset) {
        return 1f
    }
    return (1f - (offset - minOffset) / (maxOffset - minOffset)).coerceIn(0f, 1f)
}
