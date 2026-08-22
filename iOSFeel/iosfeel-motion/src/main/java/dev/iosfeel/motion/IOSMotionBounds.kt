package dev.iosfeel.motion

data class IOSMotionBounds(
    val min: Float,
    val max: Float
) {
    init {
        require(min <= max) { "min ($min) must be <= max ($max)" }
    }

    fun constrain(
        value: Float
    ): Float {
        return value.coerceIn(
            min,
            max
        )
    }
}
