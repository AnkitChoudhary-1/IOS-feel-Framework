package dev.iosfeel.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

class FrameMonitorState {

    var frameTimeMs by mutableFloatStateOf(0f)

    var approximateFps by mutableFloatStateOf(0f)
}

@Composable
fun rememberFrameMonitor(): FrameMonitorState {

    val state = remember {
        FrameMonitorState()
    }

    LaunchedEffect(Unit) {

        var previousFrame = 0L

        while (true) {

            withFrameNanos { currentFrame ->

                if (previousFrame != 0L) {

                    val difference =
                        currentFrame - previousFrame

                    val ms =
                        difference /
                            1_000_000f

                    state.frameTimeMs = ms

                    state.approximateFps =
                        if (ms > 0f) {
                            1000f / ms
                        } else {
                            0f
                        }
                }

                previousFrame = currentFrame
            }
        }
    }

    return state
}
