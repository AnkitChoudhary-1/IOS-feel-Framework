Start Phase 5B. I checked the current August 2026 Compose APIs first: LazyColumn now directly accepts both a custom FlingBehavior and an OverscrollEffect, and it applies the overscroll node internally. Compose's nested-scroll system also explicitly has pre/post scroll and pre/post fling phases, which is exactly the architecture we need.

One change from Phase 5A: don't build our own LazyColumn and don't manually translate LazyColumn content. We'll let LazyColumn remain responsible for virtualization/layout and inject iOSFeel behavior around it.

Phase 5B — Real Compose integration

By the end, we want this:

val fling =
    rememberIOSFlingBehavior()

val overscroll =
    rememberIOSOverscrollEffect()

LazyColumn(
    state = listState,
    flingBehavior = fling,
    overscrollEffect = overscroll
) {
    items(posts) {
        Post(it)
    }
}

That's dramatically better than requiring applications to use a custom list implementation.

1. Refactor IOSScrollConfig

Replace it with:

package dev.iosfeel.scroll

import androidx.compose.runtime.Immutable

@Immutable
data class IOSScrollConfig(

    // Fling
    val flingVelocityMultiplier: Float = 1.0f,
    val minimumFlingVelocity: Float = 25f,

    // Elasticity
    val resistanceFactor: Float = 0.55f,
    val resistanceExponent: Float = 0.85f,
    val maxOverscrollPx: Float = 220f,

    // Spring return
    val springStiffness: Float = 300f,
    val springDampingRatio: Float = 0.78f
) {

    init {
        require(flingVelocityMultiplier > 0f)
        require(minimumFlingVelocity >= 0f)

        require(resistanceFactor > 0f)
        require(resistanceExponent > 0f)
        require(maxOverscrollPx > 0f)

        require(springStiffness > 0f)
        require(springDampingRatio > 0f)
    }
}

Again, these are iOSFeel tuning values.

We aren't pretending they're Apple's internal parameters.

2. Replace our incomplete fling implementation

Phase 5A deliberately had:

return velocity

because we hadn't integrated real decay yet.

Now we'll use Compose's AnimationState.animateDecay(), which is specifically intended for fling-like animations and continuously updates position and velocity during decay.

Create:

IOSDecayFlingBehavior.kt
package dev.iosfeel.scroll

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import kotlin.math.abs

class IOSDecayFlingBehavior(
    private val decaySpec: DecayAnimationSpec<Float>,
    private val config: IOSScrollConfig
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {

        val startingVelocity =
            initialVelocity *
                config.flingVelocityMultiplier

        if (
            abs(startingVelocity) <
            config.minimumFlingVelocity
        ) {
            return startingVelocity
        }

        var previousValue = 0f

        val animation =
            AnimationState(
                initialValue = 0f,
                initialVelocity =
                    startingVelocity
            )

        animation.animateDecay(
            animationSpec = decaySpec
        ) {

            val delta =
                value -
                    previousValue

            val consumed =
                scrollBy(delta)

            previousValue += consumed

            /*
             * The child could not consume the
             * entire frame displacement.
             *
             * We've reached a boundary or another
             * nested participant has taken control.
             */
            if (
                kotlin.math.abs(
                    consumed - delta
                ) > 0.5f
            ) {
                cancelAnimation()
            }
        }

        /*
         * Whatever velocity remains becomes
         * available to the nested-scroll system.
         */
        return animation.velocity
    }
}

The important pipeline is now:

finger release
      ↓
initial velocity
      ↓
AnimationState
      ↓
decay physics
      ↓
frame displacement
      ↓
scrollBy(delta)
      ↓
LazyColumn

Rather than:

finger release
↓
arbitrary 400ms animation
3. Why scrollBy() consumption matters

Suppose a fling requests:

frame movement = 42 px

but the list reaches the bottom after:

17 px

Then:

requested = 42
consumed = 17
remaining = 25

We should stop pretending the list is still freely moving.

That leftover motion can become meaningful to:

overscroll
sheet parent
collapsing header
another nested-scroll parent

Compose's scrolling state and nested-scroll system are explicitly built around partial consumption like this.

4. Compose factory

Create:

RememberIOSFlingBehavior.kt
package dev.iosfeel.scroll

import androidx.compose.animation.core.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSFlingBehavior(
    config: IOSScrollConfig =
        IOSScrollConfig()
): FlingBehavior {

    val decay =
        rememberSplineBasedDecay<Float>()

    return remember(
        decay,
        config
    ) {
        IOSDecayFlingBehavior(
            decaySpec = decay,
            config = config
        )
    }
}

rememberSplineBasedDecay() is already used by current Compose scrolling APIs such as Pager as their decay-animation primitive.

Initially, we benefit from Android/Compose's mature decay implementation.

Later we can compare it against our own experimentally tuned decay.

5. Don't immediately replace spline decay

This distinction matters.

Our project goal isn't:

"Everything must be custom."

It's:

"Everything should feel excellent."

If Android's spline decay gets us:

90% there

we should tune around it before spending weeks implementing:

custom numerical integrator
custom density adaptation
custom velocity cutoffs
custom frame simulation

Only replace infrastructure when we can demonstrate an actual benefit.

6. Fling Laboratory controls

Add:

Release velocity multiplier

0.7 ────●──────── 1.6

For example:

Slider(
    value =
        config.flingVelocityMultiplier,

    onValueChange = {
        // update laboratory config
    },

    valueRange =
        0.7f..1.6f
)

Then test the same strong flick with:

0.8x
1.0x
1.15x
1.3x

You'll immediately see how important fling distance is to perceived scrolling behavior.

Do not assume higher = better.

7. Nested scrolling

Now we need something that can cooperate with future:

IOSSheet
    ↓
LazyColumn

Compose's nested scrolling has four relevant callbacks:

onPreScroll
onPostScroll
onPreFling
onPostFling

The pre phase lets a parent take input before the child, while post lets the parent react to what the child could not consume.

Create:

IOSScrollNestedConnection.kt
package dev.iosfeel.scroll

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

class IOSScrollNestedConnection(
    private val state:
        IOSScrollInteractionState
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {

        /*
         * If an existing elastic displacement
         * is being pulled back toward zero,
         * consume that first.
         */
        val consumedY =
            state.consumeOverscrollRecovery(
                available.y
            )

        return Offset(
            x = 0f,
            y = consumedY
        )
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {

        if (
            available.y == 0f
        ) {
            return Offset.Zero
        }

        /*
         * Child couldn't consume this movement.
         * This is where elastic overscroll begins.
         */
        val consumedY =
            state.consumeOverscroll(
                available.y
            )

        return Offset(
            x = 0f,
            y = consumedY
        )
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity {

        if (
            state.overscroll != 0f
        ) {
            state.releaseOverscroll(
                velocityY =
                    available.y
            )
        }

        return Velocity.Zero
    }
}

This is the key idea:

LazyColumn tries to scroll
        ↓
LazyColumn consumes what it can
        ↓
leftover delta
        ↓
onPostScroll()
        ↓
iOSFeel elasticity

That's much cleaner than manually figuring out list boundaries.

8. Create proper interaction state

Let's separate our high-level state from the actual animation.

Create:

IOSScrollInteractionState.kt
package dev.iosfeel.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class IOSScrollInteractionState(
    private val config:
        IOSScrollConfig
) {

    private val overscrollAnimation =
        Animatable(0f)

    var overscroll by
        mutableFloatStateOf(0f)
        private set

    var phase by
        mutableStateOf(
            IOSScrollPhase.Idle
        )
        private set

    val animatedOverscroll: Float
        get() =
            overscrollAnimation.value

    fun consumeOverscroll(
        delta: Float
    ): Float {

        if (delta == 0f) {
            return 0f
        }

        phase =
            IOSScrollPhase.Overscrolling

        val previous =
            overscroll

        overscroll =
            applyIOSScrollResistance(
                currentOverscroll =
                    overscroll,

                delta = delta,

                config = config
            )

        return overscroll -
            previous
    }

    fun consumeOverscrollRecovery(
        delta: Float
    ): Float {

        if (
            overscroll == 0f ||
            delta == 0f
        ) {
            return 0f
        }

        /*
         * Only consume input that moves the
         * stretch back toward zero.
         */
        val recovering =
            (
                overscroll > 0f &&
                delta < 0f
            ) ||
            (
                overscroll < 0f &&
                delta > 0f
            )

        if (!recovering) {
            return 0f
        }

        val previous =
            overscroll

        val newValue =
            when {

                overscroll > 0f ->
                    (overscroll + delta)
                        .coerceAtLeast(0f)

                else ->
                    (overscroll + delta)
                        .coerceAtMost(0f)
            }

        overscroll =
            newValue

        return newValue -
            previous
    }

    suspend fun syncAnimationToDrag() {

        overscrollAnimation.stop()

        overscrollAnimation.snapTo(
            overscroll
        )
    }

    suspend fun releaseOverscroll(
        velocityY: Float
    ) {

        if (overscroll == 0f) {
            phase =
                IOSScrollPhase.Idle

            return
        }

        phase =
            IOSScrollPhase.SpringingBack

        overscrollAnimation.stop()

        overscrollAnimation.snapTo(
            overscroll
        )

        overscrollAnimation.animateTo(
            targetValue = 0f,

            initialVelocity =
                velocityY,

            animationSpec =
                spring(
                    stiffness =
                        config.springStiffness,

                    dampingRatio =
                        config.springDampingRatio
                )
        ) {

            overscroll =
                value
        }

        overscroll = 0f

        phase =
            IOSScrollPhase.Idle
    }

    suspend fun interrupt() {

        val current =
            overscrollAnimation.value

        overscrollAnimation.stop()

        overscroll =
            current

        phase =
            IOSScrollPhase.Dragging
    }
}
9. One subtle problem

NestedScrollConnection.onPostScroll() is not suspend.

Therefore, we cannot do this directly inside it:

overscrollAnimation.snapTo(...)

every pointer frame.

That's okay.

During active dragging:

mutable Float state

is enough.

We use Animatable only when:

finger releases
→ spring back

This is a good separation:

drag:
direct state mutation

release:
animation
10. Remember the interaction state
@Composable
fun rememberIOSScrollInteractionState(
    config: IOSScrollConfig =
        IOSScrollConfig()
): IOSScrollInteractionState {

    return remember(config) {
        IOSScrollInteractionState(
            config = config
        )
    }
}
11. Applying the visual displacement

This is where we need to be careful with LazyColumn.

We do not want to move every item individually.

Wrap it:

Box(
    modifier =
        Modifier.fillMaxSize()
) {

    LazyColumn(
        modifier =
            Modifier.graphicsLayer {
                translationY =
                    interactionState.overscroll
            },
        ...
    )
}

So the entire rendered list receives one GPU translation.

Conceptually:

LazyColumn layout stays normal
        ↓
graphics layer
        ↓
entire surface shifts

instead of relayout:

every item moves through layout

That's much cheaper.

12. Compose nested-scroll wrapper

Create:

@Composable
fun IOSScrollableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState =
        rememberLazyListState(),
    config: IOSScrollConfig =
        IOSScrollConfig(),
    content:
        LazyListScope.() -> Unit
) {

    val flingBehavior =
        rememberIOSFlingBehavior(
            config
        )

    val interaction =
        rememberIOSScrollInteractionState(
            config
        )

    val nestedConnection =
        remember(interaction) {
            IOSScrollNestedConnection(
                interaction
            )
        }

    LazyColumn(
        modifier =
            modifier
                .nestedScroll(
                    nestedConnection
                )
                .graphicsLayer {
                    translationY =
                        interaction
                            .overscroll
                },

        state = state,

        flingBehavior =
            flingBehavior,

        /*
         * Temporarily disable Compose's standard
         * overscroll because iOSFeel renders ours.
         */
        overscrollEffect = null,

        content = content
    )
}

Current LazyColumn explicitly exposes both flingBehavior and overscrollEffect; passing null disables its default effect.

Now we have:

IOSScrollableLazyColumn {

    items(100) {
        Text("Item $it")
    }
}
13. But ultimately don't force developers to use our wrapper

Our long-term API should also support:

val fling =
    rememberIOSFlingBehavior()

val overscroll =
    rememberIOSOverscrollEffect()

LazyColumn(
    flingBehavior = fling,
    overscrollEffect = overscroll
) {
    ...
}

because current LazyColumn directly accepts OverscrollEffect and applies the effect's modifier node internally.

That is the cleaner final API.

The wrapper is useful right now while we develop the physics.

14. Why we aren't implementing OverscrollEffect immediately

OverscrollEffect isn't merely:

fun transformY(...)

It participates in both:

scroll input
+
fling input
+
rendering node

So I don't want to create a fake implementation that compiles only conceptually.

We'll make our nested-scroll version work first.

Then extracting it into a real IOSOverscrollEffect becomes much safer.

15. Phase changes

We need the list debug UI to display something meaningful.

During pointer movement when the list isn't at a boundary:

phase = DRAGGING

During fling:

phase = FLINGING

During stretch:

phase = OVERSCROLLING

After release:

phase = SPRINGING_BACK

Then:

IDLE

Our custom FlingBehavior can receive an optional observer.

Add:

interface IOSFlingObserver {

    fun onFlingStarted(
        velocity: Float
    )

    fun onFlingVelocityChanged(
        velocity: Float
    )

    fun onFlingEnded()
}

Then:

class IOSDecayFlingBehavior(
    private val decaySpec:
        DecayAnimationSpec<Float>,

    private val config:
        IOSScrollConfig,

    private val observer:
        IOSFlingObserver? = null
)

During the decay:

observer?.onFlingVelocityChanged(
    velocity
)

This is mostly for our Laboratory/debug tooling.

Don't force normal applications to observe every frame.

16. Very important: fling interruption

Suppose:

feed flying upward
velocity = 4200 px/s

        ↓

user puts finger down

Normal Compose scroll mutation infrastructure is useful here: starting a new user scroll can cancel the currently running scroll/fling. ScrollableState.scrollBy() and related scroll mutation APIs are designed to coordinate and cancel ongoing scroll operations safely.

So we should not invent another global mutex that fights Compose.

This is an example where using Compose's native machinery is better than taking over everything ourselves.

17. Nested-scroll behavior with future sheets

This is why Phase 5B matters.

Imagine Phase 6:

┌─────────────────────────┐
│ Comments sheet          │
│                         │
│ LazyColumn              │
│                         │
└─────────────────────────┘

User scrolls comments upward.

As long as the list can consume:

finger ↑
   ↓
LazyColumn scrolls

But when list reaches top and user pulls downward:

LazyColumn
cannot consume
      ↓
available.y remains
      ↓
onPostScroll()
      ↓
parent sheet gets delta
      ↓
sheet moves

This is exactly why Compose's nested-scroll lifecycle exposes leftover deltas and velocities.

That will make Phase 6 significantly easier.

18. High-refresh testing

Your device might refresh at:

60Hz  → 16.67ms/frame

90Hz  → 11.11ms/frame

120Hz → 8.33ms/frame

So the scroll engine should avoid doing:

❌ allocations every scroll delta
❌ new configs every frame
❌ launching coroutine for every pixel
❌ expensive recomposition every delta
❌ item-level transforms

Our high-frequency path should mostly be:

available delta
     ↓
Float math
     ↓
state update
     ↓
graphics translation

Keep it boring.

That's usually how fast code looks.

19. Performance laboratory

Add a real stress page:

Scroll Performance

Items: 1,000
Each item:
- avatar placeholder
- 3 lines text
- image placeholder

Current velocity:
4312 px/s

Overscroll:
0px

Approx frame:
8.29ms

Refresh:
120Hz

Then compare:

A — standard LazyColumn

B — IOSFling only

C — IOSFling + elastic overscroll

We need to know whether our "better feel" costs us significant jank.

20. Add Macrobenchmark later in this phase

The simple FPS monitor from Phase 1 is useful for development, but it isn't proof of performance.

Create eventually:

benchmark/
└── ScrollBenchmark.kt

Benchmark:

cold app start
 ↓
open Scroll Laboratory
 ↓
fling feed repeatedly
 ↓
collect frame timing

At minimum compare:

default LazyColumn

vs

iOSFeel LazyColumn

If:

default:
1.5% janky frames

iOSFeel:
14% janky frames

we have failed regardless of how nice the elasticity looks.

21. One improvement to our resistance equation

Our Phase 5A equation works as an experiment, but there's a weakness near maximum overscroll: resistance approaches zero very aggressively.

Let's make the resistance calculation independently inspectable:

fun calculateIOSResistanceMultiplier(
    overscroll: Float,
    config: IOSScrollConfig
): Float {

    val fraction =
        (
            kotlin.math.abs(
                overscroll
            ) /
                config.maxOverscrollPx
        ).coerceIn(
            0f,
            1f
        )

    return (
        config.resistanceFactor *
            (
                1f -
                    fraction.pow(
                        config
                            .resistanceExponent
                    )
            )
        ).coerceAtLeast(
        0.05f
    )
}

Then:

fun applyIOSScrollResistance(
    currentOverscroll: Float,
    delta: Float,
    config: IOSScrollConfig
): Float {

    val multiplier =
        calculateIOSResistanceMultiplier(
            overscroll =
                currentOverscroll,

            config = config
        )

    return (
        currentOverscroll +
            delta * multiplier
        ).coerceIn(
        -config.maxOverscrollPx,
        config.maxOverscrollPx
    )
}

Now the maximum stretch still has a tiny physical response rather than becoming mathematically dead.

22. Put resistance values in the laboratory

Show:

Overscroll    Resistance

0px           0.55
40px          0.46
80px          0.36
120px         0.26
180px         0.12
220px         0.05

This lets us see the physics we're feeling.

That will help tremendously when tuning later.

23. Tests for the fling integration

The actual Compose fling requires coroutine/UI testing, but keep pure rules testable.

For example:

@Test
fun resistanceNeverBecomesNegative() {

    val config =
        IOSScrollConfig()

    val multiplier =
        calculateIOSResistanceMultiplier(
            overscroll =
                10_000f,

            config =
                config
        )

    assertTrue(
        multiplier > 0f
    )
}

And config:

@Test(
    expected =
        IllegalArgumentException::class
)
fun invalidOverscrollDistanceFails() {

    IOSScrollConfig(
        maxOverscrollPx = -1f
    )
}
24. What not to add yet

Still don't add:

❌ pull-to-refresh
❌ collapsing Instagram profile header
❌ reels scrolling
❌ horizontal paging
❌ sheet/list coordination
❌ stretchy images

Those will make debugging impossible if the base scrolling isn't correct.

Test this first:

1000 boring rows

because it isolates the motion.

Phase 5B checkpoint

Once this is running properly, we should have:

✅ real velocity-driven decay fling
✅ Compose AnimationState integration
✅ fling cancellation at bounds
✅ remaining velocity preserved
✅ adjustable fling multiplier
✅ NestedScrollConnection
✅ pre-scroll overscroll recovery
✅ post-scroll elastic consumption
✅ post-fling spring return
✅ nonlinear resistance
✅ interruptible elasticity
✅ LazyColumn integration
✅ no custom list virtualization
✅ high-refresh-friendly rendering
✅ stress laboratory
✅ architecture ready for sheets

And the current 2026 Compose API supports exactly the injection model we're targeting: LazyColumn exposes flingBehavior and overscrollEffect, while nested scrolling provides dedicated pre/post scroll and fling passes.

Phase 5 status

At this point:

Phase 0  Foundation          ✅
Phase 1  Motion              ✅
Phase 2  Haptics             ✅
Phase 3  Gestures            ✅
Phase 4  Navigation          ✅
Phase 5  Scroll physics      ✅*

Phase 6  Sheets              ← NEXT
Phase 7  Materials / Glass
Phase 8  Components
Phase 9  Social app