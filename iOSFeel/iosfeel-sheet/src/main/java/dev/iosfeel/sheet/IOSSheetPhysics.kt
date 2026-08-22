package dev.iosfeel.sheet

import kotlin.math.abs

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
    velocityThreshold: Float = 900f,
    dismissible: Boolean = false,
    dismissVelocityThreshold: Float = 1800f
): IOSSheetTarget {
    require(detents.isNotEmpty()) { "detents list must not be empty" }

    val nearestIndex = detents.indices.minBy { index ->
        abs(detents[index].offsetPx - currentOffset)
    }

    // Dismiss condition: At lowest detent (highest offsetPx) with strong downward velocity
    if (dismissible && nearestIndex == detents.lastIndex && velocityY >= dismissVelocityThreshold) {
        return IOSSheetTarget.Dismiss
    }

    if (abs(velocityY) < velocityThreshold) {
        return IOSSheetTarget.Detent(detents[nearestIndex])
    }

    return if (velocityY > 0f) {
        // Downward throw -> move to next lower (larger offsetPx) detent
        val nextIndex = (nearestIndex + 1).coerceAtMost(detents.lastIndex)
        IOSSheetTarget.Detent(detents[nextIndex])
    } else {
        // Upward throw -> move to next higher (smaller offsetPx) detent
        val nextIndex = (nearestIndex - 1).coerceAtLeast(0)
        IOSSheetTarget.Detent(detents[nextIndex])
    }
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
