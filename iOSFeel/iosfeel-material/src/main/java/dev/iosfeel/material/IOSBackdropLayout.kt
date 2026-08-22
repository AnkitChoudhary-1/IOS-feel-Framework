package dev.iosfeel.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer

@Composable
fun IOSBackdropLayout(
    state: IOSBackdropState,
    modifier: Modifier = Modifier,
    backdrop: @Composable () -> Unit,
    overlay: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Captured Backdrop Scene
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    state.layer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(state.layer)
                }
        ) {
            backdrop()
        }

        // Overlay Glass Layers (isolated from capture to prevent feedback loop)
        overlay()
    }
}
