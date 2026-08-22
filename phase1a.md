Start Phase 1 with a single horizontal motion experiment. Android's current Compose guidance actually recommends the same core approach we want: stop an Animatable when touch begins, update it with snapTo() during dragging, use VelocityTracker, and feed release velocity into the following animation.

Phase 1 — Motion Engine

For now, your module should contain:

iosfeel-motion/
└── src/main/java/dev/iosfeel/motion/
    ├── IOSMotionPhase.kt
    ├── IOSSpringSpec.kt
    ├── IOSMotionPreset.kt
    └── IOSMotionController.kt

And the test application:

app/
└── src/main/java/dev/iosfeel/lab/
    └── MotionLaboratory.kt
1. IOSMotionPhase.kt
package dev.iosfeel.motion

enum class IOSMotionPhase {
    Idle,
    Dragging,
    Springing
}

We're explicitly tracking what the object is doing rather than treating everything as just an animation.

2. IOSSpringSpec.kt
package dev.iosfeel.motion

data class IOSSpringSpec(
    val stiffness: Float,
    val dampingRatio: Float
)
3. IOSMotionPreset.kt
package dev.iosfeel.motion

object IOSMotionPreset {

    val Snappy = IOSSpringSpec(
        stiffness = 520f,
        dampingRatio = 0.78f
    )

    val Smooth = IOSSpringSpec(
        stiffness = 320f,
        dampingRatio = 0.82f
    )

    val Gentle = IOSSpringSpec(
        stiffness = 180f,
        dampingRatio = 0.88f
    )
}

Important: these are our own experimental presets, not Apple's private values.

4. The actual motion controller

Create IOSMotionController.kt:

package dev.iosfeel.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class IOSMotionController(
    initialPosition: Float = 0f
) {

    val position = Animatable(initialPosition)

    var velocity by mutableFloatStateOf(0f)
        private set

    var target by mutableFloatStateOf(initialPosition)
        private set

    var phase by mutableStateOf(IOSMotionPhase.Idle)
        private set

    suspend fun beginDrag() {
        /*
         * Critical:
         * If we're currently springing, stop exactly where
         * the object currently is.
         */
        position.stop()

        velocity = position.velocity

        phase = IOSMotionPhase.Dragging
    }

    suspend fun dragBy(
        delta: Float,
        currentVelocity: Float
    ) {
        phase = IOSMotionPhase.Dragging

        velocity = currentVelocity

        position.snapTo(
            position.value + delta
        )
    }

    suspend fun springTo(
        targetPosition: Float,
        releaseVelocity: Float,
        spec: IOSSpringSpec = IOSMotionPreset.Smooth
    ) {

        target = targetPosition
        velocity = releaseVelocity
        phase = IOSMotionPhase.Springing

        position.animateTo(
            targetValue = targetPosition,

            animationSpec = spring(
                dampingRatio = spec.dampingRatio,
                stiffness = spec.stiffness
            ),

            initialVelocity = releaseVelocity,

            block = {
                velocity = this.velocity
            }
        )

        velocity = 0f
        phase = IOSMotionPhase.Idle
    }

    suspend fun snapTo(
        value: Float
    ) {
        position.snapTo(value)
        velocity = 0f
        target = value
        phase = IOSMotionPhase.Idle
    }
}

Animatable is useful here because Compose guarantees continuous values when an animation is interrupted, and spring-based animateTo() can also maintain velocity continuity.

5. Now build the Motion Laboratory

This is where you'll actually feel what we're making.

Create MotionLaboratory.kt:

package dev.iosfeel.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.iosfeel.motion.IOSMotionController
import dev.iosfeel.motion.IOSMotionPreset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MotionLaboratory() {

    val controller = remember {
        IOSMotionController()
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "iOSFeel — Motion Laboratory"
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = controller.position.value.roundToInt(),
                            y = 0
                        )
                    }
                    .size(
                        width = 130.dp,
                        height = 90.dp
                    )
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(26.dp)
                    )
                    .pointerInput(Unit) {

                        awaitEachGesture {

                            val velocityTracker =
                                VelocityTracker()

                            val down =
                                awaitFirstDown(
                                    requireUnconsumed = false
                                )

                            velocityTracker.addPosition(
                                down.uptimeMillis,
                                down.position
                            )

                            /*
                             * Grab the card even when it
                             * is currently springing.
                             */
                            controller.beginDrag()

                            val pointerId = down.id

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
                                        currentVelocity =
                                            currentVelocity
                                    )
                                }

                                if (change.changedToUp()) {

                                    val releaseVelocity =
                                        velocityTracker
                                            .calculateVelocity()
                                            .x

                                    scope.launch {

                                        controller.springTo(
                                            targetPosition = 0f,
                                            releaseVelocity =
                                                releaseVelocity,
                                            spec =
                                                IOSMotionPreset.Smooth
                                        )
                                    }

                                    break
                                }

                                if (!change.pressed) {
                                    break
                                }
                            }
                        }
                    }
            )
        }

        MotionDebugPanel(
            controller = controller
        )
    }
}

There's one important thing happening here:

velocityTracker.addPosition(...)

on every pointer movement.

Compose's VelocityTracker exists specifically to estimate pointer velocity from these samples. The current documentation recommends recording the down event and movement events for the best tracking.

6. Debug panel

Put this underneath the same file for now:

@Composable
private fun MotionDebugPanel(
    controller: IOSMotionController
) {

    Column(
        modifier = Modifier.padding(
            vertical = 20.dp
        )
    ) {

        Text(
            text =
                "Position: ${
                    controller.position.value.roundToInt()
                } px"
        )

        Text(
            text =
                "Velocity: ${
                    controller.velocity.roundToInt()
                } px/s"
        )

        Text(
            text =
                "Target: ${
                    controller.target.roundToInt()
                } px"
        )

        Text(
            text =
                "State: ${controller.phase}"
        )
    }
}

Now your screen will show something like:

iOSFeel — Motion Laboratory


             ┌──────────────┐
             │              │
             │              │
             └──────────────┘


Position: 173 px
Velocity: -1387 px/s
Target: 0 px
State: Springing
7. What you should test

This part matters more than staring at the code.

First, slowly drag the card right:

[ CARD ] →

Velocity:
~200 px/s

Release.

It should smoothly return.

Then throw it hard:

[ CARD ] ──────────→

Velocity:
~2500 px/s

Release.

The initial spring motion should clearly inherit that momentum.

That behavior comes from:

initialVelocity = releaseVelocity

instead of:

initialVelocity = 0f

Android's own Compose gesture-animation example uses the same basic technique: velocity is calculated from pointer movement and supplied to the following animation.

8. Now perform the important test

Throw:

CARD ─────────→

The card begins returning:

              ←──── CARD

While it's moving, touch it.

The instant you touch it:

Springing
    ↓

controller.beginDrag()

    ↓

position.stop()

    ↓

Dragging

You should then be able to move it:

← CARD →

without the card snapping back to zero first.

That's the first behavior that starts demonstrating why we're building this framework.

9. There is one bug we deliberately haven't solved yet

Consider this:

CARD returning left at

-1600 px/s

       ↓

you touch it

We currently preserve:

velocity = position.velocity

but once your finger starts dragging we rapidly replace that with finger velocity.

For our first experiment that's fine.

Later we'll improve the handoff between:

animation velocity
      ↓
touch ownership
      ↓
finger velocity

so interaction doesn't suddenly lose physical continuity.

That's one of the things we'll study rather than hiding behind a generic animation API.

Phase 1A checkpoint

Once this builds, you should be able to confirm all six:

✅ Card follows finger directly

✅ Velocity changes in real time

✅ Slow release behaves differently from fast release

✅ Release velocity enters the spring

✅ Touching during spring stops the animation

✅ Card can immediately be dragged again

If any of these six fails, don't move to buttons, navigation, blur, haptics, or scrolling.

This tiny black card is currently the most important component in the entire iOSFeel project.