Phase 5 — Scroll Physics Engine

This is one of the most important phases for the “iOS feel” goal. Scrolling is something you feel constantly, and tiny differences in deceleration, boundary resistance, fling continuation, and interruption make an app feel noticeably different.

Compose already provides strong nested-scrolling infrastructure, including pre/post scroll and fling propagation. We should build on top of that, not replace the entire Android scrolling stack.

Phase 5 architecture

Add:

iosfeel-scroll/
└── src/main/java/dev/iosfeel/scroll/
    ├── IOSScrollState.kt
    ├── IOSScrollPhase.kt
    ├── IOSScrollPhysics.kt
    ├── IOSScrollConfig.kt
    ├── IOSScrollResistance.kt
    ├── IOSFlingBehavior.kt
    ├── IOSOverscrollState.kt
    ├── IOSScrollModifier.kt
    └── RememberIOSScrollState.kt

Dependencies:

iosfeel-scroll
      │
      ├── iosfeel-core
      ├── iosfeel-motion
      └── iosfeel-haptics

The first version will be one-dimensional vertical scrolling.

1. Define scroll phases

Create IOSScrollPhase.kt:

package dev.iosfeel.scroll

enum class IOSScrollPhase {
    Idle,
    Dragging,
    Flinging,
    Overscrolling,
    SpringingBack
}

So our state knows whether content is:

finger-controlled
        ↓
Dragging

momentum-controlled
        ↓
Flinging

outside its valid bounds
        ↓
Overscrolling

returning to valid bounds
        ↓
SpringingBack
2. Define the scroll state

Create IOSScrollState.kt:

package dev.iosfeel.scroll

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class IOSScrollState {

    var phase by mutableStateOf(
        IOSScrollPhase.Idle
    )
        internal set

    var position by mutableFloatStateOf(0f)
        internal set

    var velocity by mutableFloatStateOf(0f)
        internal set

    var overscroll by mutableFloatStateOf(0f)
        internal set

    var maxScroll by mutableFloatStateOf(0f)
        internal set

    val isOverscrolled: Boolean
        get() = overscroll != 0f

    val canScrollBackward: Boolean
        get() = position > 0f

    val canScrollForward: Boolean
        get() = position < maxScroll
}

Notice the important separation:

position

is valid content position, while:

overscroll

is the temporary elastic displacement.

Don't store them as one giant value.

3. Remember API
package dev.iosfeel.scroll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSScrollState(): IOSScrollState {
    return remember {
        IOSScrollState()
    }
}

Usage:

val scrollState =
    rememberIOSScrollState()
4. Scroll configuration

Create IOSScrollConfig.kt:

package dev.iosfeel.scroll

data class IOSScrollConfig(

    val resistanceFactor: Float = 0.55f,

    val resistanceExponent: Float = 0.85f,

    val maxOverscrollPx: Float = 220f,

    val flingDecayMultiplier: Float = 1f,

    val springStiffness: Float = 300f,

    val springDampingRatio: Float = 0.78f
)

Again:

these are experimental iOSFeel values, not Apple's values.

We'll tune them in the laboratory.

5. First important piece: elastic resistance

Normal clamping feels like:

finger ↓ 100px

content ↓ 100px

boundary reached

finger ↓ another 100px

content stays completely frozen

That's not what we want.

We want:

boundary reached

finger ↓ 100px
     ↓
content ↓ 45px

finger ↓ another 100px
     ↓
content ↓ only another 25px

Resistance becomes stronger as you stretch farther.

Create IOSScrollResistance.kt:

package dev.iosfeel.scroll

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

fun applyIOSScrollResistance(
    currentOverscroll: Float,
    delta: Float,
    config: IOSScrollConfig
): Float {

    if (delta == 0f) {
        return currentOverscroll
    }

    val normalizedDistance =
        (
            abs(currentOverscroll) /
                config.maxOverscrollPx
        ).coerceIn(
            0f,
            1f
        )

    val resistance =
        config.resistanceFactor *
            (
                1f -
                    normalizedDistance
                        .pow(
                            config.resistanceExponent
                        )
            )

    val resistedDelta =
        delta * resistance

    return (
        currentOverscroll +
            resistedDelta
        ).coerceIn(
        -config.maxOverscrollPx,
        config.maxOverscrollPx
    )
}

Conceptually:

overscroll = 0

resistance relatively low
        ↓
content moves noticeably


overscroll = large

resistance becomes stronger
        ↓
content barely moves
6. Test the resistance mathematically

Create:

iosfeel-scroll/src/test/...
IOSScrollResistanceTest.kt
@Test
fun initialOverscrollMovesContent() {

    val config =
        IOSScrollConfig()

    val result =
        applyIOSScrollResistance(
            currentOverscroll = 0f,
            delta = 100f,
            config = config
        )

    assertTrue(
        result > 0f
    )

    assertTrue(
        result < 100f
    )
}

And:

@Test
fun resistanceGetsStrongerFartherFromBoundary() {

    val config =
        IOSScrollConfig()

    val near =
        applyIOSScrollResistance(
            currentOverscroll = 10f,
            delta = 50f,
            config = config
        ) - 10f

    val far =
        applyIOSScrollResistance(
            currentOverscroll = 180f,
            delta = 50f,
            config = config
        ) - 180f

    assertTrue(
        far < near
    )
}

That's important: scrolling physics should be testable without Compose.

7. Core scroll consumption

Create IOSScrollPhysics.kt.

package dev.iosfeel.scroll

data class IOSScrollResult(
    val consumed: Float,
    val unconsumed: Float
)

fun consumeIOSScrollDelta(
    state: IOSScrollState,
    delta: Float,
    config: IOSScrollConfig
): IOSScrollResult {

    if (delta == 0f) {
        return IOSScrollResult(
            consumed = 0f,
            unconsumed = 0f
        )
    }

    val proposed =
        state.position - delta

    /*
     * Inside normal scroll bounds.
     */
    if (
        proposed >= 0f &&
        proposed <= state.maxScroll
    ) {

        state.position =
            proposed

        state.overscroll = 0f

        return IOSScrollResult(
            consumed = delta,
            unconsumed = 0f
        )
    }

    /*
     * Reached top.
     */
    if (proposed < 0f) {

        state.position = 0f

        state.overscroll =
            applyIOSScrollResistance(
                currentOverscroll =
                    state.overscroll,

                delta = delta,

                config = config
            )

        return IOSScrollResult(
            consumed = delta,
            unconsumed = 0f
        )
    }

    /*
     * Reached bottom.
     */
    state.position =
        state.maxScroll

    state.overscroll =
        applyIOSScrollResistance(
            currentOverscroll =
                state.overscroll,

            delta = delta,

            config = config
        )

    return IOSScrollResult(
        consumed = delta,
        unconsumed = 0f
    )
}

This first version intentionally consumes overscroll itself.

Later nested scrolling changes this slightly.

8. Why nested scrolling matters

Imagine:

Profile page
│
├── collapsing header
│
└── LazyColumn

or:

Bottom sheet
│
└── comments LazyColumn

When the comments list reaches its top and the user keeps dragging:

child list can't scroll
        ↓
remaining delta
        ↓
sheet should begin moving

Compose's nested-scroll system is specifically designed for this. Child components can consume part of a delta and propagate the rest upward; fling velocity can also be shared.

So our engine must never greedily consume everything in every circumstance.

That becomes especially important in Phase 6.

9. Build an overscroll spring state

Create IOSOverscrollState.kt:

package dev.iosfeel.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable

@Stable
class IOSOverscrollState {

    val displacement =
        Animatable(0f)

    suspend fun dragTo(
        value: Float
    ) {
        displacement.stop()

        displacement.snapTo(
            value
        )
    }

    suspend fun springBack(
        initialVelocity: Float,
        config: IOSScrollConfig
    ) {

        displacement.animateTo(
            targetValue = 0f,

            initialVelocity =
                initialVelocity,

            animationSpec =
                spring(
                    stiffness =
                        config.springStiffness,

                    dampingRatio =
                        config.springDampingRatio
                )
        )
    }

    suspend fun stop() {
        displacement.stop()
    }
}
10. Why use another Animatable?

During finger movement:

finger owns overscroll

so:

snapTo(...)

During release:

physics owns overscroll

so:

animateTo(0)

Same pattern we've used throughout the framework.

11. Fling behavior

Compose exposes FlingBehavior specifically so custom scrollables can decide how velocity is consumed after finger release. Its default behavior represents Compose's standard natural fling curve.

For Phase 5A, don't immediately write an entire decay integrator.

Start by wrapping Compose's decay system.

Create IOSFlingBehavior.kt:

package dev.iosfeel.scroll

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import kotlin.math.abs

class IOSFlingBehavior(
    private val decaySpec:
        DecayAnimationSpec<Float>,
    private val velocityMultiplier:
        Float = 1f
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {

        var velocity =
            initialVelocity *
                velocityMultiplier

        if (
            abs(velocity) < 1f
        ) {
            return 0f
        }

        /*
         * More complete decay integration follows
         * after our laboratory is working.
         */

        return velocity
    }
}

This is intentionally incomplete.

The point is to establish the public abstraction first.

12. Don't guess fling physics blindly

Our eventual fling should preserve:

release velocity
      ↓
deceleration curve
      ↓
content displacement

rather than:

release
 ↓
"animate 500 pixels over 400ms"

The engine should care about velocity.

Example:

300 px/s

should stop quickly.

While:

7000 px/s

should travel significantly farther.

13. Build the first scroll modifier using Compose primitives

Compose's scrollable already participates in nested scrolling automatically. That's a huge advantage.

Conceptually:

val composeScrollableState =
    rememberScrollableState { delta ->

        val result =
            consumeIOSScrollDelta(
                state =
                    iosScrollState,

                delta = delta,

                config = config
            )

        result.consumed
    }

Then:

Modifier.scrollable(
    state = composeScrollableState,
    orientation = Orientation.Vertical
)

Don't write raw pointerInput for everything.

That's exactly the kind of Android infrastructure we want to retain.

14. Create a reusable container

Eventually:

@Composable
fun IOSScrollColumn(
    state: IOSScrollState =
        rememberIOSScrollState(),
    config: IOSScrollConfig =
        IOSScrollConfig(),
    content: @Composable () -> Unit
)

But for Phase 5A make an experimental:

@Composable
fun IOSScrollSurface(...)

that lays content at:

-contentPosition
+
overscroll

Conceptually:

graphicsLayer {
    translationY =
        -state.position +
            state.overscroll
}
15. Laboratory

Add:

Motion       ✅
Haptics      ✅
Gestures     ✅
Navigation   ✅
Scrolling    ✅
Sheets       soon
Materials    soon

The screen:

┌───────────────────────────┐
│ Scroll Laboratory         │
│                           │
│ Item 1                    │
│ Item 2                    │
│ Item 3                    │
│ Item 4                    │
│ ...                       │
│ Item 40                   │
│                           │
└───────────────────────────┘

Position: 426 px
Velocity: -1432 px/s
Overscroll: 0 px
Phase: FLINGING

Resistance: 0.55
Maximum stretch: 220px

At the top:

↓↓↓↓ finger

┌───────────────────────────┐
│                           │
│   elastic blank space     │
│                           │
│ Item 1                    │
│ Item 2                    │
└───────────────────────────┘

Release:

elastic region
     ↓
spring
     ↓
0
16. Make the spring interruptible

While:

Overscroll = 90px
       ↓
springing toward 0

touch again.

Immediately:

spring stops at 61px
       ↓
finger owns displacement

Not:

wait for spring

or:

snap to zero

This principle should now feel familiar.

17. Add haptics carefully

Do not vibrate just because overscroll exists.

But a meaningful threshold could be useful later:

pull ↓

120px
────── refresh threshold ─────
         *tick*

release
       ↓
perform refresh

That's where our Phase 2:

IOSHapticThreshold

becomes useful.

For ordinary elastic scrolling:

no haptic is needed.

18. Separate scroll from overscroll visuals

Current Compose's scroll pipeline treats overscroll as a distinct final stage after nested-scroll consumption, and the platform can use that leftover input for visual stretch/glow effects.

That's actually a good architectural model for us:

pointer delta
     ↓
nested pre-scroll
     ↓
actual content scroll
     ↓
nested post-scroll
     ↓
remaining delta
     ↓
iOSFeel overscroll

This is better than:

all movement
   ↓
our custom scroll engine

because it lets iOSFeel coexist with other Compose components.

19. The proper target architecture

Eventually:

Touch
  ↓
Compose scrollable
  ↓
Nested scroll
  ↓
┌──────────────────────────┐
│ IOSScrollState           │
│                         │
│ consume valid content   │
└──────────┬───────────────┘
           ↓ leftover
┌──────────────────────────┐
│ IOSOverscrollEffect      │
│                         │
│ resistance              │
│ elastic displacement    │
│ spring-back             │
└──────────────────────────┘
           ↓
rendering

This is the architecture I want us to aim for.

20. Add a pure deceleration model

Before implementing flings in Compose, create something measurable.

IOSScrollPhysics.kt:

fun calculateDeceleratedVelocity(
    velocity: Float,
    deltaSeconds: Float,
    decelerationRate: Float
): Float {

    if (deltaSeconds <= 0f) {
        return velocity
    }

    return velocity *
        kotlin.math.exp(
            -decelerationRate *
                deltaSeconds
        )
}

And distance:

fun calculateFrameDisplacement(
    velocity: Float,
    deltaSeconds: Float
): Float {

    return velocity *
        deltaSeconds
}

This gives us a simple model:

frame 1
velocity 5000

frame 2
velocity 4800

frame 3
velocity 4610

...

Rather than fixed-duration animation.

21. Test velocity behavior
@Test
fun velocityFallsOverTime() {

    val result =
        calculateDeceleratedVelocity(
            velocity = 5000f,
            deltaSeconds = 0.016f,
            decelerationRate = 3f
        )

    assertTrue(
        result < 5000f
    )

    assertTrue(
        result > 0f
    )
}

And:

@Test
fun fasterInitialVelocityTravelsFarther() {

    val slow =
        calculateFrameDisplacement(
            velocity = 500f,
            deltaSeconds = 0.016f
        )

    val fast =
        calculateFrameDisplacement(
            velocity = 5000f,
            deltaSeconds = 0.016f
        )

    assertTrue(
        fast > slow
    )
}
22. Important: don't build LazyColumn ourselves

This would be a huge mistake:

IOSLazyColumn

→ custom virtualization
→ custom recycling
→ custom measurement
→ custom prefetch
→ custom accessibility

No.

Compose's Lazy APIs already have sophisticated scrolling and nested-scroll integration.

Eventually we should aim for something more like:

LazyColumn(
    state = listState,
    flingBehavior =
        rememberIOSFlingBehavior(),
    overscrollEffect =
        rememberIOSOverscrollEffect()
)

rather than replacing LazyColumn.

That's how this framework remains realistic.

23. Phase 5A target

For this first scroll step, complete:

✅ IOSScrollState
✅ scroll phases
✅ separate content position / overscroll
✅ configurable resistance
✅ nonlinear elastic boundary
✅ overscroll limit
✅ spring-back state
✅ interruption support
✅ basic deceleration mathematics
✅ testable physics
✅ Scroll Laboratory
✅ architecture compatible with nested scrolling

We are not finished with Phase 5 yet.

Phase 5B next

Phase 5B will implement the harder integration layer:

custom FlingBehavior
+
real Compose decay animation
+
NestedScrollConnection
+
leftover velocity propagation
+
reusable OverscrollEffect
+
LazyColumn integration
+
high-refresh performance testing