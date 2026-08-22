Start Phase 1C now. This is the step where the motion prototype becomes reusable framework code instead of demo-only logic.

The main goals are: extract dragging into a reusable modifier, prevent animation/gesture coroutines from fighting each other, expose a clean motion state API, and make the system ready for future navigation/sheets.

1. Create a reusable motion state

Create:

iosfeel-motion/
└── IOSMotionState.kt
package dev.iosfeel.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Stable
class IOSMotionState(
    initialPosition: Float = 0f
) {

    private val operationMutex = Mutex()

    val position = Animatable(initialPosition)

    var phase by mutableStateOf(IOSMotionPhase.Idle)
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    var target by mutableFloatStateOf(initialPosition)
        private set

    val isAnimating: Boolean
        get() = position.isRunning

    suspend fun beginDrag() {
        /*
         * Important:
         * Don't hold the mutex while stop() waits for
         * an animation that may itself need state access.
         */
        val animationVelocity = position.velocity

        position.stop()

        operationMutex.withLock {
            velocity = animationVelocity
            phase = IOSMotionPhase.Dragging
        }
    }

    suspend fun dragBy(
        delta: Float,
        gestureVelocity: Float
    ) {
        operationMutex.withLock {
            phase = IOSMotionPhase.Dragging
            velocity = gestureVelocity

            position.snapTo(
                position.value + delta
            )
        }
    }

    suspend fun springTo(
        targetPosition: Float,
        initialVelocity: Float,
        spec: IOSSpringSpec
    ) {
        operationMutex.withLock {
            target = targetPosition
            velocity = initialVelocity
            phase = IOSMotionPhase.Springing
        }

        try {
            position.animateTo(
                targetValue = targetPosition,
                animationSpec = spring(
                    stiffness = spec.stiffness,
                    dampingRatio = spec.dampingRatio
                ),
                initialVelocity = initialVelocity
            ) {
                velocity = this.velocity
            }

            operationMutex.withLock {
                velocity = 0f
                phase = IOSMotionPhase.Idle
            }

        } catch (throwable: Throwable) {
            operationMutex.withLock {
                velocity = position.velocity
                phase = IOSMotionPhase.Cancelled
            }

            throw throwable
        }
    }

    suspend fun cancel() {
        val currentVelocity = position.velocity

        position.stop()

        operationMutex.withLock {
            velocity = currentVelocity
            phase = IOSMotionPhase.Cancelled
        }
    }

    suspend fun snapTo(
        value: Float
    ) {
        position.stop()

        operationMutex.withLock {
            position.snapTo(value)

            target = value
            velocity = 0f
            phase = IOSMotionPhase.Idle
        }
    }

    fun progressBetween(
        start: Float,
        end: Float
    ): Float {
        if (start == end) return 1f

        return (
            (position.value - start) /
                (end - start)
            ).coerceIn(0f, 1f)
    }
}

The important addition is the Mutex.

Without coordination, this can happen:

spring coroutine
       ↓
animateTo()

        +

drag coroutine
       ↓
snapTo()

        +

another spring
       ↓
animateTo()

Now three operations are trying to own the same motion state.

We want:

one active motion owner
at a time

That becomes critical later for sheets and navigation.

2. Add rememberIOSMotionState

Create:

RememberIOSMotionState.kt
package dev.iosfeel.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSMotionState(
    initialPosition: Float = 0f
): IOSMotionState {
    return remember {
        IOSMotionState(
            initialPosition = initialPosition
        )
    }
}

Now consumers use:

val motionState =
    rememberIOSMotionState()

instead of:

remember {
    IOSMotionController()
}

This starts making the API feel like Compose.

3. Create reusable drag configuration

Create:

IOSMotionDragConfig.kt
package dev.iosfeel.motion

data class IOSMotionDragConfig(
    val targetPosition: Float = 0f,
    val springSpec: IOSSpringSpec =
        IOSMotionPreset.Smooth,
    val enabled: Boolean = true
)

Later this can grow to support:

horizontal
vertical
2D
bounds
resistance
snap points

But not yet.

4. Build Modifier.iosMotionDrag

Create:

IOSMotionDragModifier.kt
package dev.iosfeel.motion

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.awaitEachGesture
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun Modifier.iosMotionDrag(
    state: IOSMotionState,
    config: IOSMotionDragConfig =
        IOSMotionDragConfig()
): Modifier {

    if (!config.enabled) {
        return this
    }

    return pointerInput(
        state,
        config
    ) {

        coroutineScope {

            awaitEachGesture {

                val tracker = VelocityTracker()

                val down = awaitFirstDown(
                    requireUnconsumed = false
                )

                tracker.addPosition(
                    down.uptimeMillis,
                    down.position
                )

                /*
                 * Important:
                 * beginDrag cancels any existing spring.
                 */
                state.beginDrag()

                val pointerId = down.id

                var releasedNormally = false

                while (true) {

                    val event =
                        awaitPointerEvent()

                    val change =
                        event.changes
                            .firstOrNull {
                                it.id == pointerId
                            }
                            ?: break

                    tracker.addPosition(
                        change.uptimeMillis,
                        change.position
                    )

                    val delta =
                        change
                            .positionChange()
                            .x

                    if (delta != 0f) {

                        change.consume()

                        val currentVelocity =
                            tracker
                                .calculateVelocity()
                                .x

                        state.dragBy(
                            delta = delta,
                            gestureVelocity =
                                currentVelocity
                        )
                    }

                    if (change.changedToUp()) {

                        releasedNormally = true

                        val releaseVelocity =
                            tracker
                                .calculateVelocity()
                                .x

                        launch {

                            state.springTo(
                                targetPosition =
                                    config.targetPosition,
                                initialVelocity =
                                    releaseVelocity,
                                spec =
                                    config.springSpec
                            )
                        }

                        break
                    }

                    if (!change.pressed) {
                        break
                    }
                }

                if (!releasedNormally) {

                    launch {

                        state.springTo(
                            targetPosition =
                                config.targetPosition,
                            initialVelocity =
                                state.velocity,
                            spec =
                                config.springSpec
                        )
                    }
                }
            }
        }
    }
}

Now the gesture code no longer belongs to the laboratory.

That is a major milestone.

5. Your laboratory becomes much simpler

The draggable card becomes:

val motionState =
    rememberIOSMotionState()

val settings =
    remember {
        IOSMotionSettings()
    }

Then:

Box(
    modifier = Modifier
        .offset {
            IntOffset(
                x = motionState
                    .position
                    .value
                    .roundToInt(),
                y = 0
            )
        }
        .size(
            width = 130.dp,
            height = 90.dp
        )
        .background(
            Color.Black,
            RoundedCornerShape(26.dp)
        )
        .iosMotionDrag(
            state = motionState,
            config = IOSMotionDragConfig(
                targetPosition = 0f,
                springSpec =
                    settings.currentSpec()
            )
        )
)

Compare that with the huge pointer handler from Phase 1A.

Your application now says:

.iosMotionDrag(...)

instead of knowing about:

VelocityTracker
pointer IDs
gesture loops
spring launching
cancellation handling

That's exactly what a framework should do.

6. This is the first reusable iOSFeel primitive

Conceptually:

Any Compose object
        ↓
Modifier.iosMotionDrag()
        ↓
IOSMotionState
        ↓
velocity tracking
        ↓
interruptible spring

So tomorrow we could use it on:

Card(...)
    .iosMotionDrag(...)

Image(...)
    .iosMotionDrag(...)

Box(...)
    .iosMotionDrag(...)

Later:

IOSSheet
   ↓
same engine

IOSNavigationStack
   ↓
same engine

That's why we're not directly building those yet.

7. Add bounds now

A framework shouldn't allow the card to disappear infinitely offscreen.

Create:

IOSMotionBounds.kt
package dev.iosfeel.motion

data class IOSMotionBounds(
    val min: Float,
    val max: Float
) {
    init {
        require(min <= max)
    }

    fun constrain(
        value: Float
    ): Float {
        return value.coerceIn(
            min,
            max
        )
    }
}

Then modify your drag config:

data class IOSMotionDragConfig(
    val targetPosition: Float = 0f,
    val springSpec: IOSSpringSpec =
        IOSMotionPreset.Smooth,
    val bounds: IOSMotionBounds? = null,
    val enabled: Boolean = true
)
8. Update dragBy

Change:

suspend fun dragBy(
    delta: Float,
    gestureVelocity: Float
)

to:

suspend fun dragBy(
    delta: Float,
    gestureVelocity: Float,
    bounds: IOSMotionBounds? = null
) {

    operationMutex.withLock {

        phase = IOSMotionPhase.Dragging

        velocity = gestureVelocity

        val proposedPosition =
            position.value + delta

        val constrainedPosition =
            bounds?.constrain(
                proposedPosition
            ) ?: proposedPosition

        position.snapTo(
            constrainedPosition
        )
    }
}

Then the modifier calls:

state.dragBy(
    delta = delta,
    gestureVelocity = currentVelocity,
    bounds = config.bounds
)
9. Use bounds in the laboratory

For example:

IOSMotionDragConfig(
    targetPosition = 0f,
    springSpec = settings.currentSpec(),
    bounds = IOSMotionBounds(
        min = -500f,
        max = 500f
    )
)

Now:

-500 px ← CARD → +500 px

is its allowed region.

10. But normal bounds aren't enough for iOS-like behavior

Eventually we do not want:

drag → hit boundary → instantly stop

because polished interfaces often have resistance.

Think:

normal area

finger 100 px
↓
object 100 px

but past boundary:

finger 100 px
↓
object maybe 40 px

Then:

more drag
↓
even stronger resistance

That will become part of the scrolling/sheet work.

For Phase 1, hard bounds are enough.

11. Add lifecycle callbacks

Future components will need to know:

drag started
drag changed
released
settled

Update config:

data class IOSMotionDragConfig(
    val targetPosition: Float = 0f,
    val springSpec: IOSSpringSpec =
        IOSMotionPreset.Smooth,
    val bounds: IOSMotionBounds? = null,
    val enabled: Boolean = true,

    val onDragStarted: (() -> Unit)? = null,
    val onReleased: ((Float) -> Unit)? = null
)

Then:

config.onDragStarted?.invoke()

state.beginDrag()

And on release:

config.onReleased?.invoke(
    releaseVelocity
)

Later:

IOSSheet(
    onDetentChanged = { ... }
)

can build on the same principles.

12. Add tests for bounds

Create:

class IOSMotionBoundsTest {

    @Test
    fun valueInsideBoundsIsUnchanged() {

        val bounds =
            IOSMotionBounds(
                min = -100f,
                max = 100f
            )

        assertEquals(
            50f,
            bounds.constrain(50f)
        )
    }

    @Test
    fun valueAboveMaximumIsClamped() {

        val bounds =
            IOSMotionBounds(
                min = -100f,
                max = 100f
            )

        assertEquals(
            100f,
            bounds.constrain(500f)
        )
    }

    @Test
    fun valueBelowMinimumIsClamped() {

        val bounds =
            IOSMotionBounds(
                min = -100f,
                max = 100f
            )

        assertEquals(
            -100f,
            bounds.constrain(-500f)
        )
    }
}
Phase 1 is now effectively complete

You should now have this architecture:

iosfeel-motion

IOSMotionState
      │
      ├── position
      ├── velocity
      ├── target
      ├── phase
      ├── interruption
      └── spring

IOSMotionDragConfig

IOSMotionBounds

Modifier.iosMotionDrag()
      │
      ├── touch tracking
      ├── VelocityTracker
      ├── cancellation
      └── release → spring

And the app only does:

Modifier.iosMotionDrag(...)

That's our first genuine reusable framework API.

Phase 1 completion checklist
✅ draggable motion
✅ real touch velocity
✅ velocity-preserving release
✅ spring physics
✅ interrupt animation
✅ re-grab moving object
✅ cancellation recovery
✅ tunable spring presets
✅ configurable bounds
✅ reusable Compose Modifier
✅ clean state object
✅ basic concurrency protection
✅ debug laboratory
✅ initial tests