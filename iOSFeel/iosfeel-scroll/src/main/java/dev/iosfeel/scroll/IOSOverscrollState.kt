package dev.iosfeel.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable

@Stable
class IOSOverscrollState {

    val displacement = Animatable(0f)

    suspend fun dragTo(
        value: Float
    ) {
        displacement.stop()
        displacement.snapTo(value)
    }

    suspend fun springBack(
        initialVelocity: Float,
        config: IOSScrollConfig
    ) {
        displacement.animateTo(
            targetValue = 0f,
            initialVelocity = initialVelocity,
            animationSpec = spring(
                stiffness = config.springStiffness,
                dampingRatio = config.springDampingRatio
            )
        )
    }

    suspend fun stop() {
        displacement.stop()
    }
}
