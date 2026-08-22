Start Phase 1B now.

The goal is to turn the first draggable-card prototype into a proper motion system with a cleaner state machine, tunable spring values, cancellation handling, and basic performance observation.

1. Replace the motion phase enum

Update IOSMotionPhase.kt:

package dev.iosfeel.motion

enum class IOSMotionPhase {
    Idle,
    Dragging,
    Springing,
    Cancelled
}
2. Improve the spring spec

Update IOSSpringSpec.kt:

package dev.iosfeel.motion

data class IOSSpringSpec(
    val stiffness: Float,
    val dampingRatio: Float
) {
    init {
        require(stiffness > 0f)
        require(dampingRatio > 0f)
    }
}

This prevents nonsense like:

stiffness = -200f

from entering our engine.

3. Replace IOSMotionController

Use this version:

package dev.iosfeel.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class IOSMotionController(
    initialPosition: Float = 0f
) {

    val position = Animatable(initialPosition)

    var phase by mutableStateOf(IOSMotionPhase.Idle)
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    var target by mutableFloatStateOf(initialPosition)
        private set

    val isRunning: Boolean
        get() = position.isRunning

    suspend fun beginDrag() {

        /*
         * Read current animation velocity BEFORE stopping.
         */
        val inheritedVelocity = position.velocity

        position.stop()

        velocity = inheritedVelocity

        phase = IOSMotionPhase.Dragging
    }

    suspend fun dragBy(
        delta: Float,
        gestureVelocity: Float
    ) {

        if (phase != IOSMotionPhase.Dragging) {
            phase = IOSMotionPhase.Dragging
        }

        velocity = gestureVelocity

        position.snapTo(
            position.value + delta
        )
    }

    suspend fun springTo(
        targetPosition: Float,
        initialVelocity: Float,
        springSpec: IOSSpringSpec
    ) {

        target = targetPosition

        velocity = initialVelocity

        phase = IOSMotionPhase.Springing

        try {

            position.animateTo(
                targetValue = targetPosition,

                animationSpec = spring(
                    stiffness = springSpec.stiffness,
                    dampingRatio = springSpec.dampingRatio
                ),

                initialVelocity = initialVelocity
            ) {

                velocity = this.velocity
            }

            velocity = 0f

            phase = IOSMotionPhase.Idle

        } catch (exception: Exception) {

            /*
             * Animation interruption is expected
             * during user interaction.
             */

            velocity = position.velocity
            phase = IOSMotionPhase.Cancelled

            throw exception
        }
    }

    suspend fun cancel() {

        if (position.isRunning) {

            velocity = position.velocity

            position.stop()
        }

        phase = IOSMotionPhase.Cancelled
    }

    suspend fun reset() {

        position.stop()

        position.snapTo(0f)

        target = 0f
        velocity = 0f

        phase = IOSMotionPhase.Idle
    }
}

There is one subtle principle here:

animation velocity
        ↓
user touches
        ↓
capture velocity
        ↓
stop animation
        ↓
finger gains ownership

We don't reset physics just because the user touched the object.

4. Create configurable spring state

Create:

IOSMotionSettings.kt
package dev.iosfeel.motion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
5. Add tuning controls

Now your laboratory becomes more useful.

Add these imports:

import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

Then inside MotionLaboratory():

val settings = remember {
    IOSMotionSettings()
}

Change:

controller.springTo(
    targetPosition = 0f,
    releaseVelocity = releaseVelocity,
    spec = IOSMotionPreset.Smooth
)

to:

controller.springTo(
    targetPosition = 0f,
    initialVelocity = releaseVelocity,
    springSpec = settings.currentSpec()
)

Now add this under the debug panel:

SpringControls(
    settings = settings,
    onReset = {
        scope.launch {
            controller.reset()
        }
    }
)
6. Create SpringControls
@Composable
private fun SpringControls(
    settings: IOSMotionSettings,
    onReset: () -> Unit
) {

    Column {

        Text(
            text = "Spring stiffness: ${
                settings.stiffness.roundToInt()
            }"
        )

        Slider(
            value = settings.stiffness,
            onValueChange = {
                settings.stiffness = it
            },
            valueRange = 100f..800f
        )

        Text(
            text = "Damping: ${
                "%.2f".format(settings.dampingRatio)
            }"
        )

        Slider(
            value = settings.dampingRatio,
            onValueChange = {
                settings.dampingRatio = it
            },
            valueRange = 0.3f..1.2f
        )

        Row {

            Button(
                onClick = {
                    settings.useSnappy()
                }
            ) {
                Text("Snappy")
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Button(
                onClick = {
                    settings.useSmooth()
                }
            ) {
                Text("Smooth")
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Button(
                onClick = {
                    settings.useGentle()
                }
            ) {
                Text("Gentle")
            }
        }

        Button(
            onClick = onReset,
            modifier = Modifier.padding(
                top = 12.dp
            )
        ) {
            Text("Reset")
        }
    }
}

Now you can actually experiment.

Try:

Stiffness = 150
Damping = 0.60

Then:

Stiffness = 700
Damping = 0.95

You'll immediately feel how different the same interaction becomes.

7. Handle gesture cancellation

This is important.

Imagine:

finger down
↓
dragging
↓
Android cancels pointer

Perhaps another gesture wins.

We should not leave:

phase = Dragging

forever.

Update the loop logic.

Instead of only detecting:

if (!change.pressed) {
    break
}

keep track of whether we actually received a valid release.

For example:

var releasedNormally = false

while (true) {

    val event = awaitPointerEvent()

    val change =
        event.changes.firstOrNull {
            it.id == pointerId
        } ?: break

    velocityTracker.addPosition(
        change.uptimeMillis,
        change.position
    )

    val delta =
        change.positionChange().x

    if (delta != 0f) {

        change.consume()

        val currentVelocity =
            velocityTracker
                .calculateVelocity()
                .x

        controller.dragBy(
            delta = delta,
            gestureVelocity = currentVelocity
        )
    }

    if (change.changedToUp()) {

        releasedNormally = true

        val releaseVelocity =
            velocityTracker
                .calculateVelocity()
                .x

        scope.launch {

            controller.springTo(
                targetPosition = 0f,
                initialVelocity = releaseVelocity,
                springSpec = settings.currentSpec()
            )
        }

        break
    }

    if (!change.pressed) {
        break
    }
}

After the loop:

if (!releasedNormally) {

    scope.launch {

        controller.springTo(
            targetPosition = 0f,
            initialVelocity = controller.velocity,
            springSpec = settings.currentSpec()
        )
    }
}

Now even an interrupted gesture can recover gracefully.

8. Add motion progress

Eventually navigation needs:

0.0
0.1
0.2
...
1.0

So let's introduce the concept now.

Add to IOSMotionController:

fun progressBetween(
    start: Float,
    end: Float
): Float {

    if (start == end) {
        return 1f
    }

    return (
        (position.value - start) /
            (end - start)
    ).coerceIn(
        0f,
        1f
    )
}

Later navigation can do things like:

gesture progress = 0.42

and use that same value for:

page position
background position
opacity
navigation title
material transformation

This is foundational for Phase 4.

9. Add simple frame observation

Create:

FrameMonitor.kt

inside the app module.

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

Then:

val frameMonitor =
    rememberFrameMonitor()

Show:

Text(
    "Frame: ${
        "%.2f".format(frameMonitor.frameTimeMs)
    } ms"
)

Text(
    "Approx FPS: ${
        frameMonitor.approximateFps.roundToInt()
    }"
)

Don't treat this as a professional benchmark.

It's just our Laboratory indicator.

Later we'll use Macrobenchmark/JankStats properly.

10. Your Motion Laboratory should now resemble
iOSFeel Motion Laboratory


           ┌──────────────┐
           │              │
           │   DRAG ME    │
           │              │
           └──────────────┘


Position: 203 px
Velocity: -1422 px/s
Target: 0
State: SPRINGING

Frame: 8.40 ms
FPS: 119


Stiffness
──────────●────
320

Damping
──────●────────
0.82

[Snappy] [Smooth] [Gentle]

[Reset]

Now we have something genuinely useful for experimentation.

11. Tests

Create:

iosfeel-motion/
└── src/test/java/dev/iosfeel/motion/

For Phase 1, don't try unit-testing Animatable deeply yet.

Instead test our simpler rules first.

Create IOSSpringSpecTest.kt:

package dev.iosfeel.motion

import org.junit.Assert.assertEquals
import org.junit.Test

class IOSSpringSpecTest {

    @Test
    fun smoothPresetContainsExpectedValues() {

        assertEquals(
            320f,
            IOSMotionPreset.Smooth.stiffness
        )

        assertEquals(
            0.82f,
            IOSMotionPreset.Smooth.dampingRatio
        )
    }

    @Test(
        expected =
            IllegalArgumentException::class
    )
    fun negativeStiffnessThrows() {

        IOSSpringSpec(
            stiffness = -1f,
            dampingRatio = 0.8f
        )
    }
}

Simple, but we're establishing test infrastructure early.

Phase 1 status

After completing this section:

Phase 1

✅ Position
✅ Velocity
✅ Spring target
✅ Motion state
✅ Interrupt animation
✅ Re-grab animation
✅ Preserve release velocity
✅ Gesture cancellation recovery
✅ Tunable stiffness
✅ Tunable damping
✅ Motion presets
✅ Debug panel
✅ Basic frame monitor
✅ Initial unit tests