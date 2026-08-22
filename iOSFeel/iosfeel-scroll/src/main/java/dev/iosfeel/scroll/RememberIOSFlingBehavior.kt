package dev.iosfeel.scroll

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSFlingBehavior(
    config: IOSScrollConfig = IOSScrollConfig(),
    observer: IOSFlingObserver? = null
): FlingBehavior {
    val decay = rememberSplineBasedDecay<Float>()

    return remember(decay, config, observer) {
        IOSDecayFlingBehavior(
            decaySpec = decay,
            config = config,
            observer = observer
        )
    }
}
