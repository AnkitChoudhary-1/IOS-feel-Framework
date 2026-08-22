Phase 4 — Interactive Navigation

This is the phase where iOSFeel should finally start feeling different.

One important adjustment from our original plan: on modern Android we should support both our custom left-edge recognizer and Android's official Predictive Back progress API. Compose's PredictiveBackHandler gives us continuous 0f..1f progress during the system back gesture, and Google recommends migrating custom back experiences to predictive back.

Our first target is simple:

Screen A
   ↓ tap

Screen B

Then:

finger starts at left edge
        ↓
Screen B follows finger →
        ↓
Screen A appears underneath
        ↓
release
   ┌────┴────┐
   ↓         ↓
complete   cancel
   ↓         ↓
Screen A   Screen B
          springs back

Do not build a complete NavController replacement yet.

1. Create the navigation module

Add:

iosfeel-navigation/
└── src/main/java/dev/iosfeel/navigation/
    ├── IOSNavigationState.kt
    ├── IOSNavigationEntry.kt
    ├── IOSNavigationStack.kt
    ├── IOSNavigationTransition.kt
    ├── IOSBackTransitionState.kt
    └── RememberIOSNavigationState.kt

Dependencies:

iosfeel-navigation
       │
       ├── iosfeel-core
       ├── iosfeel-motion
       ├── iosfeel-gesture
       └── iosfeel-haptics

This is the first module that intentionally combines our previous engines.

2. Create navigation entries

Start extremely simply.

IOSNavigationEntry.kt:

package dev.iosfeel.navigation

import androidx.compose.runtime.Composable

data class IOSNavigationEntry(
    val key: String,
    val content: @Composable () -> Unit
)

Eventually we'll support:

arguments
saved state
deep links
transitions
screen metadata

Not now.

3. Create navigation state

IOSNavigationState.kt:

package dev.iosfeel.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf

@Stable
class IOSNavigationState(
    initialEntry: IOSNavigationEntry
) {

    private val _entries =
        mutableStateListOf(initialEntry)

    val entries: List<IOSNavigationEntry>
        get() = _entries

    val current: IOSNavigationEntry
        get() = _entries.last()

    val previous: IOSNavigationEntry?
        get() = _entries
            .getOrNull(_entries.lastIndex - 1)

    val canGoBack: Boolean
        get() = _entries.size > 1

    fun push(
        entry: IOSNavigationEntry
    ) {
        _entries.add(entry)
    }

    fun pop(): Boolean {

        if (!canGoBack) {
            return false
        }

        _entries.removeAt(
            _entries.lastIndex
        )

        return true
    }
}

Then:

@Composable
fun rememberIOSNavigationState(
    initialEntry: IOSNavigationEntry
): IOSNavigationState {

    return remember {
        IOSNavigationState(
            initialEntry
        )
    }
}

For now:

stack:

[ Home ]

then:

[ Home ]
[ Profile ]
4. Navigation transition state

Now create the interesting part.

IOSBackTransitionState.kt:

package dev.iosfeel.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class IOSBackTransitionPhase {
    Idle,
    Interactive,
    Completing,
    Cancelling
}

@Stable
class IOSBackTransitionState {

    val progress =
        Animatable(0f)

    var phase by mutableStateOf(
        IOSBackTransitionPhase.Idle
    )
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    val isInteractive: Boolean
        get() =
            phase ==
                IOSBackTransitionPhase.Interactive

    suspend fun begin() {

        progress.stop()

        phase =
            IOSBackTransitionPhase.Interactive
    }

    suspend fun update(
        value: Float,
        gestureVelocity: Float
    ) {

        phase =
            IOSBackTransitionPhase.Interactive

        velocity =
            gestureVelocity

        progress.snapTo(
            value.coerceIn(
                0f,
                1f
            )
        )
    }

    suspend fun reset() {

        progress.stop()
        progress.snapTo(0f)

        velocity = 0f

        phase =
            IOSBackTransitionPhase.Idle
    }
}

Think of:

progress.value

as the master parameter for the whole transition.

5. Don't animate everything separately

This is extremely important.

Don't create:

currentScreenAnimation
previousScreenAnimation
navbarAnimation
shadowAnimation

running independently.

Instead:

                    progress
                       │
          ┌────────────┼────────────┐
          ↓            ↓            ↓
 current screen    previous      shadow
 translation       screen
                   translation

One interaction controls everything.

For example:

progress = 0.00

Current:
x = 0%

Previous:
x = -25%

At:

progress = 0.50

Current:
x = 50%

Previous:
x = -12.5%

At:

progress = 1.00

Current:
x = 100%

Previous:
x = 0%

That coherence is what we want.

6. Create the transition calculations

IOSNavigationTransition.kt:

package dev.iosfeel.navigation

data class IOSNavigationTransform(
    val currentTranslationFraction: Float,
    val previousTranslationFraction: Float,
    val shadowAlpha: Float
)

fun calculateIOSBackTransform(
    progress: Float
): IOSNavigationTransform {

    val p =
        progress.coerceIn(
            0f,
            1f
        )

    return IOSNavigationTransform(

        currentTranslationFraction =
            p,

        previousTranslationFraction =
            -0.25f +
                (0.25f * p),

        shadowAlpha =
            1f - p
    )
}

Again:

These are our tuning values.

We are not claiming Instagram/iOS uses exactly -0.25.

Later we'll tune them.

7. Build the first navigation stack

Create IOSNavigationStack.kt:

package dev.iosfeel.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun IOSNavigationStack(
    state: IOSNavigationState,
    backTransition:
        IOSBackTransitionState,
    modifier: Modifier = Modifier
) {

    var widthPx by remember {
        mutableFloatStateOf(0f)
    }

    val progress =
        backTransition.progress.value

    val transform =
        calculateIOSBackTransform(
            progress
        )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                widthPx =
                    it.width.toFloat()
            }
    ) {

        val previous =
            state.previous

        /*
         * Only render previous screen
         * while there is somewhere to go back.
         */
        if (previous != null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {

                        translationX =
                            widthPx *
                            transform
                                .previousTranslationFraction
                    }
            ) {

                previous.content()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {

                    translationX =
                        widthPx *
                        transform
                            .currentTranslationFraction
                }
        ) {

            state.current.content()
        }
    }
}

This gives us the essential stack:

Previous Screen
       ↓
Current Screen

rather than navigating away and losing the previous screen immediately.

8. Connect our Phase 3 edge gesture

Create an interactive wrapper around IOSNavigationStack.

Something like:

val gesture =
    rememberIOSGestureState()

val transition =
    remember {
        IOSBackTransitionState()
    }

val scope =
    rememberCoroutineScope()

Then:

Modifier.iosEdgeSwipe(
    state = gesture,
    edgeWidthPx = edgeWidthPx,
    progressDistancePx = screenWidthPx,

    onStarted = {

        if (navigation.canGoBack) {

            scope.launch {
                transition.begin()
            }
        }
    },

    onChanged = {

        scope.launch {

            transition.update(
                value =
                    gesture.progress,

                gestureVelocity =
                    gesture.velocityX
            )
        }
    },

    onEnded = {

        // decision comes next
    }
)

Now:

finger movement
     ↓
gesture.progress
     ↓
transition.progress
     ↓
graphicsLayer.translationX

The page literally follows the finger.

9. Release decision

Use our Phase 3 logic:

val decision =
    decideDirectionalGestureCompletion(

        progress =
            gesture.progress,

        velocity =
            gesture.velocityX,

        direction =
            IOSGestureAxisDirection.Positive,

        thresholds =
            IOSGestureThresholds(
                progressThreshold = 0.42f,
                velocityThresholdPxPerSecond =
                    1100f
            )
    )

Again, these are iOSFeel tuning values, not Apple values.

Then:

progress = .67
velocity = 200

→ COMPLETE

or:

progress = .18
velocity = 1900

→ COMPLETE

but:

progress = .22
velocity = 300

→ CANCEL
10. Complete the transition

Add to IOSBackTransitionState:

suspend fun complete(
    initialVelocity: Float,
    spec: IOSSpringSpec =
        IOSMotionPreset.Snappy
) {

    phase =
        IOSBackTransitionPhase.Completing

    progress.animateTo(
        targetValue = 1f,

        animationSpec = spring(
            stiffness =
                spec.stiffness,

            dampingRatio =
                spec.dampingRatio
        ),

        initialVelocity =
            initialVelocity
    )
}

But there's an important issue:

our gesture velocity is:

pixels / second

while transition progress is:

0 → 1

We cannot directly pass 1900 px/s as progress velocity.

We must normalize it.

11. Normalize velocity

Create:

fun normalizeGestureVelocity(
    velocityPxPerSecond: Float,
    distancePx: Float
): Float {

    if (distancePx <= 0f) {
        return 0f
    }

    return velocityPxPerSecond /
        distancePx
}

Example:

screen distance = 1080px

velocity = 2160 px/s

becomes:

progress velocity = 2.0 / second

Much better.

Then:

val normalizedVelocity =
    normalizeGestureVelocity(
        gesture.velocityX,
        screenWidthPx
    )
12. Cancel the transition

Add:

suspend fun cancel(
    initialVelocity: Float,
    spec: IOSSpringSpec =
        IOSMotionPreset.Smooth
) {

    phase =
        IOSBackTransitionPhase.Cancelling

    progress.animateTo(
        targetValue = 0f,

        animationSpec = spring(
            stiffness =
                spec.stiffness,

            dampingRatio =
                spec.dampingRatio
        ),

        initialVelocity =
            initialVelocity
    )

    phase =
        IOSBackTransitionPhase.Idle
}

Now release logic is:

when (decision) {

    IOSGestureDecision.Complete -> {

        scope.launch {

            transition.complete(
                initialVelocity =
                    normalizedVelocity
            )

            navigation.pop()

            transition.reset()
        }
    }

    IOSGestureDecision.Cancel -> {

        scope.launch {

            transition.cancel(
                initialVelocity =
                    normalizedVelocity
            )
        }
    }
}

And that's our first real interactive navigation.

13. What it should feel like

Drag slowly:

┌──────────── Screen B ────────────┐
│                                  │
│                                  │
└──────────────────────────────────┘

finger →

At 30%:

       ┌──── Screen B ────────────┐
       │                          │
[ Screen A becoming visible ]     │
       │                          │
       └──────────────────────────┘

Release early:

        ← spring back

Screen B returns

Flick quickly:

Screen B ─────────────→

Screen A becomes current
14. Add a transition shadow

The current screen needs visual separation from the previous screen.

You could initially use a very simple edge overlay rather than expensive blur.

Conceptually:

Previous | shadow | Current

Create:

Box(
    modifier = Modifier
        .width(16.dp)
        .fillMaxHeight()
        .graphicsLayer {
            alpha =
                transform.shadowAlpha *
                    0.15f
        }
)

Don't spend much time beautifying it yet.

Phase 7 handles materials/rendering.

15. Add haptic threshold

Remember Phase 2?

When:

CANCEL
   ↓
COMPLETE

trigger:

haptics.perform(
    IOSHapticEvent
        .ThresholdActivated
)

And if the user drags back:

COMPLETE
   ↓
CANCEL

use:

haptics.perform(
    IOSHapticEvent
        .ThresholdDeactivated
)

Now:

finger
 ↓
page movement
 ↓
completion boundary
 ↓
*tick*

This is exactly why we built haptics separately.

16. Support Android Predictive Back too

Our custom left-edge recognizer is useful for experimentation, but modern Android already supplies system-back gesture progress through PredictiveBackHandler. Google specifically recommends predictive-back support, and Android 15+ enables its system animations by default when apps use supported back APIs.

For the real framework, add another input source:

PredictiveBackHandler(
    enabled = navigation.canGoBack
) { events ->

    try {

        transition.begin()

        events.collect { event ->

            transition.update(
                value =
                    event.progress,

                gestureVelocity = 0f
            )
        }

        transition.complete(
            initialVelocity = 0f
        )

        navigation.pop()

        transition.reset()

    } catch (
        cancellation:
            CancellationException
    ) {

        transition.cancel(
            initialVelocity = 0f
        )
    }
}

PredictiveBackHandler provides a flow of back events whose progress runs from 0 to 1, specifically so custom UI can react continuously to the system back gesture.

This gives iOSFeel two paths:

              Back interaction

           ┌────────┴─────────┐
           ↓                  ↓

iOSFeel edge swipe      Android system back
           │                  │
           ↓                  ↓
Gesture Engine      PredictiveBackHandler
           └────────┬─────────┘
                    ↓
          IOSBackTransitionState
                    ↓
              same renderer

That's a much better architecture than fighting Android's system navigation.

17. Build the Navigation Laboratory

Create two fake screens.

Screen A
@Composable
fun NavigationHome(
    onOpenProfile: () -> Unit
) {

    Box(
        Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {

        Button(
            onClick = onOpenProfile
        ) {
            Text(
                "Open Profile"
            )
        }
    }
}
Screen B
@Composable
fun ProfileDemo() {

    Box(
        Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {

        Text(
            "Profile"
        )
    }
}

Start with:

Home

[ Open Profile ]

Tap it:

Profile

Now left-edge drag.

That's the first proper proof that the architecture works.

18. Add live debugging

Display:

Back Transition

Gesture phase: Changed
Progress:      0.382
Velocity:      1284 px/s
Normalized:    1.19 /s

Decision:
COMPLETE

Transition:
INTERACTIVE

Frame:
8.2 ms

This is still a laboratory.

We want observability more than beauty.

19. Add tests

Test transforms:

@Test
fun halfProgressMovesCurrentHalfway() {

    val transform =
        calculateIOSBackTransform(
            0.5f
        )

    assertEquals(
        0.5f,
        transform.currentTranslationFraction
    )
}

Previous screen:

@Test
fun previousScreenReachesZeroAtCompletion() {

    val transform =
        calculateIOSBackTransform(
            1f
        )

    assertEquals(
        0f,
        transform.previousTranslationFraction
    )
}

Velocity normalization:

@Test
fun velocityIsNormalizedByDistance() {

    assertEquals(
        2f,
        normalizeGestureVelocity(
            velocityPxPerSecond = 2000f,
            distancePx = 1000f
        )
    )
}
20. Do NOT build these yet

Avoid:

❌ elaborate navigation bars
❌ giant titles
❌ shared-element transitions
❌ glass headers
❌ tab navigation
❌ modal navigation
❌ 15 different transitions

First make this one gesture excellent:

Screen A
   ↓
Screen B
   ↓
swipe-back

If that doesn't feel good, more components won't solve it.

Phase 4A target

At this point we want:

✅ two-screen back stack
✅ previous screen remains rendered
✅ current screen follows finger
✅ previous screen moves with progress
✅ single master transition progress
✅ real velocity
✅ progress + velocity completion
✅ release spring
✅ cancellation spring
✅ threshold haptic
✅ custom edge gesture
✅ Android Predictive Back input
✅ reusable transition calculations