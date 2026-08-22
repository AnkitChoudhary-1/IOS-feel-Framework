package dev.iosfeel.gesture

enum class IOSGestureDecision {
    Complete,
    Cancel
}

enum class IOSGestureAxisDirection {
    Positive,
    Negative
}

data class IOSGestureThresholds(
    val progressThreshold: Float = 0.5f,
    val velocityThresholdPxPerSecond: Float = 1200f
)

fun decideGestureCompletion(
    progress: Float,
    velocity: Float,
    thresholds: IOSGestureThresholds = IOSGestureThresholds()
): IOSGestureDecision {
    val progressPass = progress >= thresholds.progressThreshold
    val velocityPass = velocity >= thresholds.velocityThresholdPxPerSecond

    return if (progressPass || velocityPass) {
        IOSGestureDecision.Complete
    } else {
        IOSGestureDecision.Cancel
    }
}

fun decideDirectionalGestureCompletion(
    progress: Float,
    velocity: Float,
    direction: IOSGestureAxisDirection,
    thresholds: IOSGestureThresholds = IOSGestureThresholds()
): IOSGestureDecision {
    val directionalVelocity = when (direction) {
        IOSGestureAxisDirection.Positive -> velocity
        IOSGestureAxisDirection.Negative -> -velocity
    }

    return decideGestureCompletion(
        progress = progress,
        velocity = directionalVelocity,
        thresholds = thresholds
    )
}
