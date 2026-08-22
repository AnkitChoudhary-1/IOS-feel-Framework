package dev.iosfeel.sheet

import androidx.compose.runtime.Immutable
import dev.iosfeel.motion.IOSMotionPreset
import dev.iosfeel.motion.IOSSpringSpec

enum class IOSSheetImeBehavior {
    KeepDetent,
    ExpandToLarge
}

@Immutable
data class IOSSheetConfig(
    val dismissible: Boolean = true,
    val dismissOnScrimTap: Boolean = true,
    val dismissVelocityThreshold: Float = 1800f,
    val velocityThreshold: Float = 900f,
    val useImePadding: Boolean = true,
    val useImeNestedScroll: Boolean = false,
    val imeBehavior: IOSSheetImeBehavior = IOSSheetImeBehavior.KeepDetent,
    val showGrabber: Boolean = true,
    val springSpec: IOSSpringSpec = IOSMotionPreset.Smooth,
    val cornerRadiusDp: Float = 28f,
    val scrimAlphaMax: Float = 0.35f,
    val backgroundScaleMin: Float = 0.97f
) {
    init {
        require(velocityThreshold > 0f) { "velocityThreshold must be > 0" }
        require(dismissVelocityThreshold > 0f) { "dismissVelocityThreshold must be > 0" }
        require(scrimAlphaMax in 0f..1f) { "scrimAlphaMax must be in 0..1" }
        require(backgroundScaleMin in 0f..1f) { "backgroundScaleMin must be in 0..1" }
    }
}
