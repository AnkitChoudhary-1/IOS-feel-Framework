package dev.iosfeel.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class IOSMotionSettings {

    var stiffness by mutableFloatStateOf(
        IOSMotionPreset.Smooth.stiffness
    )

    var dampingRatio by mutableFloatStateOf(
        IOSMotionPreset.Smooth.dampingRatio
    )

    fun currentSpec(): IOSSpringSpec {
        return IOSSpringSpec(
            stiffness = stiffness,
            dampingRatio = dampingRatio
        )
    }

    fun useSnappy() {
        stiffness = IOSMotionPreset.Snappy.stiffness
        dampingRatio = IOSMotionPreset.Snappy.dampingRatio
    }

    fun useSmooth() {
        stiffness = IOSMotionPreset.Smooth.stiffness
        dampingRatio = IOSMotionPreset.Smooth.dampingRatio
    }

    fun useGentle() {
        stiffness = IOSMotionPreset.Gentle.stiffness
        dampingRatio = IOSMotionPreset.Gentle.dampingRatio
    }
}

@Composable
fun rememberIOSMotionSettings(): IOSMotionSettings {
    return remember {
        IOSMotionSettings()
    }
}
