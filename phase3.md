Start Phase 3: Gesture Engine.

The goal is to stop treating every interaction as raw pointer math inside each component. We want reusable gesture state that future navigation, sheets, sliders, and dismiss gestures can share.

The architecture becomes:

App / Components
      ↓
Gesture Engine
      ↓
Motion Engine + Haptics
      ↓
Compose pointer APIs
      ↓
Android input system
1. Create the module

Add:

iosfeel-gesture/
└── src/main/java/dev/iosfeel/gesture/
    ├── IOSGesturePhase.kt
    ├── IOSGestureDirection.kt
    ├── IOSGestureState.kt
    ├── IOSGestureConfig.kt
    ├── IOSGestureDecision.kt
    ├── IOSGestureThresholds.kt
    ├── IOSDragGestureModifier.kt
    ├── IOSEdgeSwipeModifier.kt
    └── RememberIOSGestureState.kt

Dependency direction:

iosfeel-gesture
      ↓
 iosfeel-core

Do not make the core gesture module depend directly on navigation or sheets.

Later those modules will depend on gesture.

2. Gesture phases

Create IOSGesturePhase.kt:

package dev.iosfeel.gesture

enum class IOSGesturePhase {
    Idle,
    Possible,
    Began,
    Changed,
    Ended,
    Cancelled
}

This gives us a proper lifecycle:

finger touches
    ↓
Possible
    ↓
movement accepted
    ↓
Began
    ↓
Changed
    ↓
Ended

or:

Possible
    ↓
another gesture wins
    ↓
Cancelled
3. Gesture direction

Create:

package dev.iosfeel.gesture

enum class IOSGestureDirection {
    Horizontal,
    Vertical,
    Any
}

Eventually we may distinguish:

Left
Right
Up
Down

but for now the axis is enough.

4. Gesture state

Create IOSGestureState.kt:

package dev.iosfeel.gesture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class IOSGestureState {

    var phase by mutableStateOf(
        IOSGesturePhase.Idle
    )
        internal set

    var translationX by mutableFloatStateOf(0f)
        internal set

    var translationY by mutableFloatStateOf(0f)
        internal set

    var velocityX by mutableFloatStateOf(0f)
        internal set

    var velocityY by mutableFloatStateOf(0f)
        internal set

    var progress by mutableFloatStateOf(0f)
        internal set

    fun reset() {
        phase = IOSGesturePhase.Idle

        translationX = 0f
        translationY = 0f

        velocityX = 0f
        velocityY = 0f

        progress = 0f
    }
}

This becomes the central readable state.

A future navigation transition could simply observe:

gesture.progress

instead of knowing anything about pointer events.

5. Remember API

Create:

package dev.iosfeel.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSGestureState(): IOSGestureState {
    return remember {
        IOSGestureState()
    }
}

Usage:

val gesture =
    rememberIOSGestureState()
6. Gesture configuration

Create IOSGestureConfig.kt:

package dev.iosfeel.gesture

data class IOSGestureConfig(
    val direction: IOSGestureDirection =
        IOSGestureDirection.Any,

    val activationSlopPx: Float = 12f,

    val progressDistancePx: Float = 300f,

    val enabled: Boolean = true
)

progressDistancePx means:

0 px       → progress 0.0
150 px     → progress 0.5
300 px     → progress 1.0

Later, components can calculate it based on screen width.

7. Normalized progress

Create a helper:

internal fun calculateGestureProgress(
    translation: Float,
    distance: Float
): Float {

    if (distance <= 0f) {
        return 0f
    }

    return (
        translation /
            distance
    ).coerceIn(
        0f,
        1f
    )
}

This is very important.

A component should ideally operate on:

0.00 → 1.00

rather than:

187 pixels

For navigation:

progress = 0.42

could mean simultaneously:

screen translation    = 42%
previous screen reveal = 42%
navbar interpolation   = 42%
shadow fading           = 42%
8. Direction locking

Suppose the user wants to swipe horizontally:

x movement = 40px
y movement = 5px

Clearly horizontal.

But:

x = 8
y = 35

should probably belong to vertical scrolling.

Create:

internal fun shouldAcceptGesture(
    dx: Float,
    dy: Float,
    config: IOSGestureConfig
): Boolean {

    return when (
        config.direction
    ) {

        IOSGestureDirection.Horizontal ->
            kotlin.math.abs(dx) >
                kotlin.math.abs(dy)

        IOSGestureDirection.Vertical ->
            kotlin.math.abs(dy) >
                kotlin.math.abs(dx)

        IOSGestureDirection.Any ->
            true
    }
}

This is our first simple gesture competition mechanism.

Later we'll make it more sophisticated.

9. Build the reusable drag modifier

Create IOSDragGestureModifier.kt:

package dev.iosfeel.gesture

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.awaitEachGesture
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlin.math.abs

fun Modifier.iosGesture(
    state: IOSGestureState,
    config: IOSGestureConfig =
        IOSGestureConfig(),
    onStarted: (() -> Unit)? = null,
    onChanged: ((IOSGestureState) -> Unit)? = null,
    onEnded: ((IOSGestureState) -> Unit)? = null,
    onCancelled: (() -> Unit)? = null
): Modifier {

    if (!config.enabled) {
        return this
    }

    return pointerInput(
        state,
        config
    ) {

        awaitEachGesture {

            state.reset()

            state.phase =
                IOSGesturePhase.Possible

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

            val start =
                down.position

            val pointerId =
                down.id

            var gestureAccepted =
                false

            var endedNormally =
                false

            while (true) {

                val event =
                    awaitPointerEvent()

                val change =
                    event.changes
                        .firstOrNull {
                            it.id == pointerId
                        }
                        ?: break

                velocityTracker.addPosition(
                    change.uptimeMillis,
                    change.position
                )

                val totalX =
                    change.position.x -
                        start.x

                val totalY =
                    change.position.y -
                        start.y

                if (!gestureAccepted) {

                    val movedEnough =
                        abs(totalX) >=
                            config.activationSlopPx ||
                        abs(totalY) >=
                            config.activationSlopPx

                    if (movedEnough) {

                        gestureAccepted =
                            shouldAcceptGesture(
                                dx = totalX,
                                dy = totalY,
                                config = config
                            )

                        if (!gestureAccepted) {

                            state.phase =
                                IOSGesturePhase.Cancelled

                            onCancelled?.invoke()

                            break
                        }

                        state.phase =
                            IOSGesturePhase.Began

                        onStarted?.invoke()
                    }
                }

                if (gestureAccepted) {

                    val velocity =
                        velocityTracker
                            .calculateVelocity()

                    state.translationX =
                        totalX

                    state.translationY =
                        totalY

                    state.velocityX =
                        velocity.x

                    state.velocityY =
                        velocity.y

                    val progressSource =
                        when (
                            config.direction
                        ) {

                            IOSGestureDirection.Horizontal ->
                                totalX

                            IOSGestureDirection.Vertical ->
                                totalY

                            IOSGestureDirection.Any ->
                                kotlin.math.sqrt(
                                    totalX * totalX +
                                        totalY * totalY
                                )
                        }

                    state.progress =
                        calculateGestureProgress(
                            translation =
                                progressSource,

                            distance =
                                config.progressDistancePx
                        )

                    state.phase =
                        IOSGesturePhase.Changed

                    change.consume()

                    onChanged?.invoke(
                        state
                    )
                }

                if (change.changedToUp()) {

                    endedNormally = true

                    if (gestureAccepted) {

                        val finalVelocity =
                            velocityTracker
                                .calculateVelocity()

                        state.velocityX =
                            finalVelocity.x

                        state.velocityY =
                            finalVelocity.y

                        state.phase =
                            IOSGesturePhase.Ended

                        onEnded?.invoke(
                            state
                        )
                    }

                    break
                }

                if (!change.pressed) {
                    break
                }
            }

            if (
                !endedNormally &&
                gestureAccepted
            ) {

                state.phase =
                    IOSGesturePhase.Cancelled

                onCancelled?.invoke()
            }
        }
    }
}

Now a component can do:

Modifier.iosGesture(
    state = gesture,
    config = IOSGestureConfig(
        direction =
            IOSGestureDirection.Horizontal
    )
)

without manually handling pointers.

10. Completion decisions

Now comes the important part.

Suppose:

progress = 0.60
velocity = 100 px/s

Probably complete.

But:

progress = 0.15
velocity = 2500 px/s

A fast flick might also deserve completion.

So create IOSGestureDecision.kt:

package dev.iosfeel.gesture

enum class IOSGestureDecision {
    Complete,
    Cancel
}

Then:

data class IOSGestureThresholds(
    val progressThreshold: Float = 0.5f,
    val velocityThresholdPxPerSecond: Float = 1200f
)

And:

fun decideGestureCompletion(
    progress: Float,
    velocity: Float,
    thresholds: IOSGestureThresholds =
        IOSGestureThresholds()
): IOSGestureDecision {

    val progressPass =
        progress >=
            thresholds.progressThreshold

    val velocityPass =
        velocity >=
            thresholds.velocityThresholdPxPerSecond

    return if (
        progressPass ||
        velocityPass
    ) {
        IOSGestureDecision.Complete
    } else {
        IOSGestureDecision.Cancel
    }
}

Now the decision logic is separate from rendering.

That's important for testing.

11. Direction-aware completion

For a rightward swipe, positive velocity matters.

A giant leftward velocity shouldn't complete it.

Add:

enum class IOSGestureAxisDirection {
    Positive,
    Negative
}

Then:

fun decideDirectionalGestureCompletion(
    progress: Float,
    velocity: Float,
    direction: IOSGestureAxisDirection,
    thresholds: IOSGestureThresholds =
        IOSGestureThresholds()
): IOSGestureDecision {

    val directionalVelocity =
        when (direction) {

            IOSGestureAxisDirection.Positive ->
                velocity

            IOSGestureAxisDirection.Negative ->
                -velocity
        }

    return decideGestureCompletion(
        progress = progress,
        velocity = directionalVelocity,
        thresholds = thresholds
    )
}

Now Phase 4 swipe-back can use:

IOSGestureAxisDirection.Positive

for a left-edge swipe moving right.

12. Build the edge-swipe primitive

Create IOSEdgeSwipeModifier.kt.

package dev.iosfeel.gesture

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

data class IOSEdgeSwipeConfig(
    val edgeWidthPx: Float = 48f,
    val progressDistancePx: Float = 300f,
    val enabled: Boolean = true
)

Instead of duplicating the entire recognizer, add an edge requirement to our base configuration.

Extend IOSGestureConfig:

data class IOSGestureConfig(
    val direction: IOSGestureDirection =
        IOSGestureDirection.Any,

    val activationSlopPx: Float = 12f,

    val progressDistancePx: Float = 300f,

    val requiredStartMaxX: Float? = null,

    val enabled: Boolean = true
)

Then after awaitFirstDown():

val maxStartX =
    config.requiredStartMaxX

if (
    maxStartX != null &&
    down.position.x > maxStartX
) {
    state.phase =
        IOSGesturePhase.Cancelled

    return@awaitEachGesture
}

Now edge swipe becomes:

fun Modifier.iosEdgeSwipe(
    state: IOSGestureState,
    edgeWidthPx: Float,
    progressDistancePx: Float,
    onStarted: (() -> Unit)? = null,
    onChanged: ((IOSGestureState) -> Unit)? = null,
    onEnded: ((IOSGestureState) -> Unit)? = null,
    onCancelled: (() -> Unit)? = null
): Modifier {

    return iosGesture(
        state = state,

        config = IOSGestureConfig(
            direction =
                IOSGestureDirection.Horizontal,

            progressDistancePx =
                progressDistancePx,

            requiredStartMaxX =
                edgeWidthPx
        ),

        onStarted = onStarted,
        onChanged = onChanged,
        onEnded = onEnded,
        onCancelled = onCancelled
    )
}

That's our first reusable interactive-back style recognizer.

13. Create a Gesture Laboratory

Add to the sample app:

Motion       ✅
Haptics      ✅
Gestures     ✅
Navigation   soon
Scrolling    soon

Create a screen with a card:

┌─────────────────────────────┐
│ Gesture Laboratory          │
│                             │
│ |← start swipe here         │
│                             │
│        ┌─────────┐          │
│        │ CONTENT │          │
│        └─────────┘          │
│                             │
│ Phase: Changed              │
│ X: 142 px                   │
│ Y: 4 px                     │
│ Velocity X: 1340 px/s       │
│ Progress: 0.47              │
│                             │
│ Decision: CANCEL            │
└─────────────────────────────┘

Use:

val gesture =
    rememberIOSGestureState()

Then:

Box(
    modifier = Modifier
        .fillMaxSize()
        .iosEdgeSwipe(
            state = gesture,
            edgeWidthPx = 64.dp.toPx(),
            progressDistancePx = screenWidth
        )
)

Use with(LocalDensity.current) to convert DP to pixels.

14. Display the completion decision live

You can calculate:

val decision =
    decideDirectionalGestureCompletion(
        progress =
            gesture.progress,

        velocity =
            gesture.velocityX,

        direction =
            IOSGestureAxisDirection.Positive
    )

Then show:

Progress: 0.24
Velocity: 340 px/s
Decision: CANCEL

and after a fast flick:

Progress: 0.19
Velocity: 1900 px/s
Decision: COMPLETE

This is exactly the logic Phase 4 navigation will need.

15. Connect haptics

When the completion decision changes:

CANCEL
  ↓
COMPLETE

trigger:

haptics.perform(
    IOSHapticEvent.ThresholdActivated
)

When moving back below it:

haptics.perform(
    IOSHapticEvent.ThresholdDeactivated
)

So now we have:

finger
   ↓
Gesture Engine
   ↓
progress + velocity
   ↓
completion decision
   ↓
Haptic Engine

The systems are beginning to work together.

16. Important: don't fire haptic every frame

Track:

var previousDecision by remember {
    mutableStateOf(
        IOSGestureDecision.Cancel
    )
}

Then:

if (
    decision != previousDecision
) {

    when (decision) {

        IOSGestureDecision.Complete ->
            haptics.perform(
                IOSHapticEvent
                    .ThresholdActivated
            )

        IOSGestureDecision.Cancel ->
            haptics.perform(
                IOSHapticEvent
                    .ThresholdDeactivated
            )
    }

    previousDecision =
        decision
}

Now you get a tactile threshold rather than buzzing continuously.

17. Unit tests

Create:

iosfeel-gesture/
└── src/test/java/dev/iosfeel/gesture/

Test progress:

@Test
fun halfDistanceProducesHalfProgress() {

    assertEquals(
        0.5f,
        calculateGestureProgress(
            translation = 150f,
            distance = 300f
        )
    )
}

Test completion:

@Test
fun largeProgressCompletesGesture() {

    val result =
        decideGestureCompletion(
            progress = 0.7f,
            velocity = 100f
        )

    assertEquals(
        IOSGestureDecision.Complete,
        result
    )
}

Test velocity:

@Test
fun highVelocityCompletesShortGesture() {

    val result =
        decideGestureCompletion(
            progress = 0.15f,
            velocity = 1800f
        )

    assertEquals(
        IOSGestureDecision.Complete,
        result
    )
}

Test opposite direction:

@Test
fun oppositeVelocityDoesNotComplete() {

    val result =
        decideDirectionalGestureCompletion(
            progress = 0.1f,
            velocity = -2000f,
            direction =
                IOSGestureAxisDirection.Positive
        )

    assertEquals(
        IOSGestureDecision.Cancel,
        result
    )
}
18. What Phase 3 gives us

We now have:

IOSGestureState
      │
      ├── translation
      ├── velocity
      ├── progress
      └── lifecycle

IOSGesture recognizer
      │
      ├── slop
      ├── direction locking
      ├── cancellation
      └── velocity tracking

IOSEdgeSwipe
      │
      └── edge-start requirement

Gesture decision
      │
      ├── progress threshold
      └── velocity threshold

Combined with earlier phases:

Gesture Engine
      ↓
progress + velocity

Motion Engine
      ↓
interruptible spring

Haptic Engine
      ↓
threshold feedback
Phase 3 completion checklist
✅ reusable gesture state
✅ gesture lifecycle
✅ horizontal/vertical locking
✅ activation slop
✅ translation tracking
✅ real velocity tracking
✅ normalized progress
✅ cancellation
✅ edge-swipe recognition
✅ progress-based completion
✅ velocity-based completion
✅ directional completion
✅ threshold haptics
✅ unit-testable decision logic