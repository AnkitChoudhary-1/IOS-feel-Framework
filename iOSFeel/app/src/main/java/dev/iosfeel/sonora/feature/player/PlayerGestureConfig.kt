package dev.iosfeel.sonora.feature.player

import androidx.compose.runtime.Immutable
import dev.iosfeel.components.expandable.IOSExpandableSurfaceConfig

@Immutable
data class PlayerGestureConfig(
    val expansionThreshold: Float = 0.5f,
    val velocityThreshold: Float = 1.15f
) {
    fun toExpandableConfig(): IOSExpandableSurfaceConfig {
        return IOSExpandableSurfaceConfig(
            expansionThreshold = expansionThreshold,
            velocityThreshold = velocityThreshold
        )
    }
}

fun decidePlayerTarget(
    progress: Float,
    velocity: Float,
    config: PlayerGestureConfig = PlayerGestureConfig()
): Float {
    if (velocity >= config.velocityThreshold) {
        return 1f
    }
    if (velocity <= -config.velocityThreshold) {
        return 0f
    }
    return if (progress >= config.expansionThreshold) {
        1f
    } else {
        0f
    }
}
