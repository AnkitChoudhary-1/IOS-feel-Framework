package dev.iosfeel.haptics

class IOSHapticDetents(
    detents: List<Float>
) {

    private val detents = detents.sorted()

    private var currentIndex: Int? = null

    fun update(
        position: Float,
        haptics: IOSHaptics
    ) {

        val nearestIndex = detents.indices.minByOrNull { index ->
            kotlin.math.abs(detents[index] - position)
        }

        if (
            nearestIndex != null &&
            nearestIndex != currentIndex
        ) {

            if (currentIndex != null) {
                haptics.selection()
            }

            currentIndex = nearestIndex
        }
    }

    fun reset() {
        currentIndex = null
    }
}
