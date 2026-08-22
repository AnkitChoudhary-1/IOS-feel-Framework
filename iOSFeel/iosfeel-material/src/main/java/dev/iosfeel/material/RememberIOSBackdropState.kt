package dev.iosfeel.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalGraphicsContext

@Composable
fun rememberIOSBackdropState(): IOSBackdropState {
    val graphicsContext = LocalGraphicsContext.current
    val layer = remember(graphicsContext) {
        graphicsContext.createGraphicsLayer()
    }

    DisposableEffect(layer, graphicsContext) {
        onDispose {
            graphicsContext.releaseGraphicsLayer(layer)
        }
    }

    return remember(layer) {
        IOSBackdropState(layer = layer)
    }
}
