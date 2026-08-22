package dev.iosfeel.sheet

sealed interface IOSSheetDetent {

    val id: String

    data object Compact : IOSSheetDetent {
        override val id = "compact"
    }

    data object Medium : IOSSheetDetent {
        override val id = "medium"
    }

    data object Large : IOSSheetDetent {
        override val id = "large"
    }

    data class Fraction(
        override val id: String,
        val fraction: Float
    ) : IOSSheetDetent {
        init {
            require(fraction in 0f..1f) { "Detent fraction must be between 0.0 and 1.0" }
        }
    }
}

data class IOSResolvedDetent(
    val detent: IOSSheetDetent,
    val offsetPx: Float
)

fun interface IOSSheetDetentResolver {
    fun resolve(
        detent: IOSSheetDetent,
        containerWidthPx: Float,
        containerHeightPx: Float
    ): Float
}

object IOSDefaultDetentResolver : IOSSheetDetentResolver {
    override fun resolve(
        detent: IOSSheetDetent,
        containerWidthPx: Float,
        containerHeightPx: Float
    ): Float {
        return when (detent) {
            IOSSheetDetent.Large -> containerHeightPx * 0.08f
            IOSSheetDetent.Medium -> containerHeightPx * 0.45f
            IOSSheetDetent.Compact -> containerHeightPx * 0.78f
            is IOSSheetDetent.Fraction -> containerHeightPx * (1f - detent.fraction)
        }
    }
}

fun resolveSheetDetents(
    containerHeightPx: Float,
    detents: List<IOSSheetDetent>,
    containerWidthPx: Float = 0f,
    resolver: IOSSheetDetentResolver = IOSDefaultDetentResolver
): List<IOSResolvedDetent> {
    require(detents.isNotEmpty()) { "detents list must not be empty" }

    return detents.map { detent ->
        val offset = resolver.resolve(
            detent = detent,
            containerWidthPx = containerWidthPx,
            containerHeightPx = containerHeightPx
        )

        IOSResolvedDetent(
            detent = detent,
            offsetPx = offset
        )
    }.sortedBy { it.offsetPx }
}

fun findResolvedDetent(
    detent: IOSSheetDetent,
    resolved: List<IOSResolvedDetent>
): IOSResolvedDetent? {
    return resolved.firstOrNull { it.detent.id == detent.id }
}
