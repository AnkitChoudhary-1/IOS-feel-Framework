package dev.iosfeel.components.expandable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSExpandableSurfaceState(
    initialProgress: Float = 0f,
    config: IOSExpandableSurfaceConfig = IOSExpandableSurfaceConfig()
): IOSExpandableSurfaceState {
    return remember {
        IOSExpandableSurfaceState(
            initialProgress = initialProgress,
            config = config
        )
    }
}
