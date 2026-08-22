package dev.iosfeel.motion

data class IOSSpringSpec(
    val stiffness: Float,
    val dampingRatio: Float
) {
    init {
        require(stiffness > 0f) { "stiffness ($stiffness) must be > 0" }
        require(dampingRatio > 0f) { "dampingRatio ($dampingRatio) must be > 0" }
    }
}
