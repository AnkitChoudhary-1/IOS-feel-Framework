package dev.iosfeel.sheet.detent

import androidx.compose.runtime.Immutable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.detent.IOSDetentResolver

/**
 * Semantic detent levels for bottom sheets in iOSFeel V2.
 */
@ExperimentalIOSFeelV2Api
sealed interface IOSSheetDetent {
    val id: String

    data object Compact : IOSSheetDetent {
        override val id: String = "compact"
    }

    data object Content : IOSSheetDetent {
        override val id: String = "content"
    }

    data object Medium : IOSSheetDetent {
        override val id: String = "medium"
    }

    data object Large : IOSSheetDetent {
        override val id: String = "large"
    }

    @Immutable
    data class Fraction(override val id: String, val fraction: Float) : IOSSheetDetent

    data object Hidden : IOSSheetDetent {
        override val id: String = "hidden"
    }
}

/**
 * Resolves semantic [IOSSheetDetent] specifications into physical pixel offsets.
 */
@ExperimentalIOSFeelV2Api
object IOSSheetDetentResolver {

    fun resolveDetents(
        detents: List<IOSSheetDetent>,
        containerHeightPx: Float,
        contentHeightPx: Float = 0f
    ): List<IOSDetent<IOSSheetDetent>> {
        return detents.map { detent ->
            val offset = when (detent) {
                is IOSSheetDetent.Large -> (containerHeightPx * 0.08f).coerceAtLeast(0f)
                is IOSSheetDetent.Medium -> containerHeightPx * 0.45f
                is IOSSheetDetent.Compact -> containerHeightPx * 0.78f
                is IOSSheetDetent.Content -> (containerHeightPx - contentHeightPx).coerceIn(0f, containerHeightPx)
                is IOSSheetDetent.Fraction -> containerHeightPx * (1f - detent.fraction).coerceIn(0f, 1f)
                is IOSSheetDetent.Hidden -> containerHeightPx
            }
            IOSDetent(
                value = offset,
                key = detent
            )
        }.sortedBy { it.value }
    }

    fun resolveTarget(
        offsetPx: Float,
        velocityPxPerSec: Float,
        resolvedDetents: List<IOSDetent<IOSSheetDetent>>
    ): IOSDetent<IOSSheetDetent> {
        val decision = IOSDetentResolver.resolve(
            position = offsetPx,
            velocity = velocityPxPerSec,
            detents = resolvedDetents,
            velocityThreshold = 400f
        )
        return decision.target
    }
}
