Start Phase 4B. This is about turning the proof-of-concept transition into navigation infrastructure that survives real app behavior.

One correction before the code: for screen state, Compose's SaveableStateHolder is exactly what we want. It is designed to preserve rememberSaveable state for navigation-like content even while that screen temporarily leaves composition.

1. Stop storing Composables directly in navigation state

Our Phase 4A:

data class IOSNavigationEntry(
    val key: String,
    val content: @Composable () -> Unit
)

was okay for a prototype, but it's not a good long-term navigation model.

Change it to:

package dev.iosfeel.navigation

import androidx.compose.runtime.Immutable

@Immutable
data class IOSNavigationEntry(
    val key: String,
    val route: String
)

So:

Navigation state
    ↓
only describes WHERE we are

Renderer
    ↓
decides WHAT Composable represents that route

This also makes restoration much easier.

2. Upgrade the navigation state

Replace IOSNavigationState.kt:

package dev.iosfeel.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf

@Stable
class IOSNavigationState internal constructor(
    initialEntries: List<IOSNavigationEntry>
) {

    private val _entries =
        mutableStateListOf<IOSNavigationEntry>()
            .apply {
                addAll(initialEntries)
            }

    val entries: List<IOSNavigationEntry>
        get() = _entries

    val current: IOSNavigationEntry
        get() = _entries.last()

    val previous: IOSNavigationEntry?
        get() = _entries
            .getOrNull(_entries.lastIndex - 1)

    val canGoBack: Boolean
        get() = _entries.size > 1

    val size: Int
        get() = _entries.size

    fun push(
        entry: IOSNavigationEntry
    ) {
        require(
            _entries.none {
                it.key == entry.key
            }
        ) {
            "Navigation entry keys must be unique."
        }

        _entries.add(entry)
    }

    fun pop(): IOSNavigationEntry? {

        if (!canGoBack) {
            return null
        }

        return _entries.removeAt(
            _entries.lastIndex
        )
    }

    internal fun snapshotKeys(): List<String> {
        return _entries.map {
            "${it.key}|${it.route}"
        }
    }
}

Now a stack can contain:

home
 ↓
profile/42
 ↓
post/932
 ↓
comments/932

rather than just two screens.

3. Make the stack saveable

Navigation-related state is one of the things Android specifically recommends preserving across recreation, but only store the minimum identifiers necessary rather than large screen objects.

Create:

IOSNavigationSaver.kt
package dev.iosfeel.navigation

import androidx.compose.runtime.saveable.Saver

internal val IOSNavigationStateSaver =
    Saver<IOSNavigationState, List<String>>(

        save = { state ->
            state.snapshotKeys()
        },

        restore = { values ->

            val entries =
                values.map { encoded ->

                    val separator =
                        encoded.indexOf('|')

                    IOSNavigationEntry(
                        key =
                            encoded.substring(
                                0,
                                separator
                            ),

                        route =
                            encoded.substring(
                                separator + 1
                            )
                    )
                }

            IOSNavigationState(
                initialEntries = entries
            )
        }
    )

Then:

@Composable
fun rememberIOSNavigationState(
    initialEntry: IOSNavigationEntry
): IOSNavigationState {

    return rememberSaveable(
        saver = IOSNavigationStateSaver
    ) {
        IOSNavigationState(
            initialEntries =
                listOf(initialEntry)
        )
    }
}

Now screen rotation shouldn't suddenly send the user back to Home.

And because rememberSaveable can restore state across activity/process recreation using saved-instance state, this is a much stronger base than plain remember.

4. Give each screen its own saved-state container

Inside our stack:

val stateHolder =
    rememberSaveableStateHolder()

Then instead of:

content(entry)

use:

stateHolder.SaveableStateProvider(
    key = entry.key
) {
    content(entry)
}

Why?

Imagine Profile has:

val scrollState =
    rememberLazyListState()

You navigate:

Profile
scroll position = 840px

↓ open post

Post

↓ swipe back

Profile

We don't want:

scroll position = 0

SaveableStateHolder exists specifically for navigation-style cases like preserving scroll state of screens that leave composition and later return.

5. Separate transition direction

Phase 4A only knew:

back

We now need:

push →
← pop

Create:

enum class IOSNavigationTransitionType {
    None,
    Push,
    InteractivePop
}

Update transition state:

var type by mutableStateOf(
    IOSNavigationTransitionType.None
)
    private set
6. Refactor the back transition state

Now make interruption a first-class behavior.

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

    suspend fun beginInteractive() {

        /*
         * Capture animation velocity BEFORE stopping.
         */
        velocity =
            progress.velocity

        progress.stop()

        phase =
            IOSBackTransitionPhase.Interactive
    }

    suspend fun updateInteractive(
        progressValue: Float,
        progressVelocity: Float
    ) {

        phase =
            IOSBackTransitionPhase.Interactive

        velocity =
            progressVelocity

        progress.snapTo(
            progressValue.coerceIn(
                0f,
                1f
            )
        )
    }

    suspend fun complete(
        initialVelocity: Float,
        spec: IOSSpringSpec =
            IOSMotionPreset.Snappy
    ) {

        progress.stop()

        phase =
            IOSBackTransitionPhase.Completing

        velocity =
            initialVelocity

        progress.animateTo(
            targetValue = 1f,

            initialVelocity =
                initialVelocity,

            animationSpec =
                spring(
                    stiffness =
                        spec.stiffness,

                    dampingRatio =
                        spec.dampingRatio
                )
        ) {
            velocity =
                this.velocity
        }
    }

    suspend fun cancel(
        initialVelocity: Float,
        spec: IOSSpringSpec =
            IOSMotionPreset.Smooth
    ) {

        progress.stop()

        phase =
            IOSBackTransitionPhase.Cancelling

        velocity =
            initialVelocity

        progress.animateTo(
            targetValue = 0f,

            initialVelocity =
                initialVelocity,

            animationSpec =
                spring(
                    stiffness =
                        spec.stiffness,

                    dampingRatio =
                        spec.dampingRatio
                )
        ) {
            velocity =
                this.velocity
        }

        phase =
            IOSBackTransitionPhase.Idle

        velocity = 0f
    }

    suspend fun reset() {

        progress.stop()

        progress.snapTo(0f)

        velocity = 0f

        phase =
            IOSBackTransitionPhase.Idle
    }
}
7. Now re-grabbing can work

Consider:

Swipe right
    ↓
release too early
    ↓
Screen starts springing ←
    ↓
user touches edge AGAIN

We don't want:

wait until animation finishes

We want:

spring position = 0.31
spring velocity = -0.8 /s
        ↓
touch
        ↓
stop spring at exactly 0.31
        ↓
interactive gesture owns progress

That's why:

progress.stop()

is called when a new interactive transition begins.

This is the same principle we established back in Phase 1.

8. Fix a subtle re-grab problem

Imagine the cancelled animation currently sits at:

progress = 0.32

Then the new gesture starts.

The user's finger has only moved:

0.03

If we do:

progress = gesture.progress

we jump:

0.32 → 0.03

Terrible.

We need a gesture base progress.

Create:

var interactionStartProgress
    by mutableFloatStateOf(0f)

When gesture begins:

interactionStartProgress =
    transition.progress.value

Then during dragging:

val remaining =
    1f -
        interactionStartProgress

val interactiveProgress =
    interactionStartProgress +
        gesture.progress *
        remaining

Example:

spring currently:
0.32

new drag:
0.00

becomes:

transition:
0.32

Then finger moves 50% of remaining distance:

0.32 + (0.50 × 0.68)

= 0.66

No jump.

9. Create an interaction coordinator

Don't put all this logic inside the UI.

Create:

IOSInteractiveBackController.kt
package dev.iosfeel.navigation

import dev.iosfeel.gesture.IOSGestureAxisDirection
import dev.iosfeel.gesture.IOSGestureDecision
import dev.iosfeel.gesture.IOSGestureThresholds
import dev.iosfeel.gesture.decideDirectionalGestureCompletion
import kotlin.math.max

class IOSInteractiveBackController(
    private val transition:
        IOSBackTransitionState
) {

    private var startProgress =
        0f

    suspend fun begin() {

        transition.beginInteractive()

        startProgress =
            transition.progress.value
    }

    suspend fun update(
        gestureProgress: Float,
        velocityPxPerSecond: Float,
        distancePx: Float
    ) {

        val remaining =
            1f - startProgress

        val mappedProgress =
            startProgress +
                gestureProgress *
                remaining

        val normalizedVelocity =
            normalizeGestureVelocity(
                velocityPxPerSecond =
                    velocityPxPerSecond,

                distancePx =
                    max(
                        distancePx,
                        1f
                    )
            )

        transition.updateInteractive(
            progressValue =
                mappedProgress,

            progressVelocity =
                normalizedVelocity
        )
    }

    fun decide(
        velocityPxPerSecond: Float,
        distancePx: Float,
        thresholds:
            IOSGestureThresholds
    ): IOSGestureDecision {

        /*
         * Use actual transition progress here,
         * not just the new gesture's local progress.
         */
        return decideDirectionalGestureCompletion(
            progress =
                transition.progress.value,

            velocity =
                velocityPxPerSecond,

            direction =
                IOSGestureAxisDirection.Positive,

            thresholds =
                thresholds
        )
    }
}

Now our navigation stack doesn't have to understand the re-grab math.

10. Multi-screen rendering

Suppose the stack is:

Home
 ↓
Profile
 ↓
Post
 ↓
Comments

During back from Comments, render only:

Post
+
Comments

Not all four screens.

Otherwise complex apps could keep enormous UI trees alive.

So:

val current =
    navigation.current

val previous =
    navigation.previous

Only those two participate in the transition.

After pop:

Comments removed

Current = Post
Previous = Profile
11. Upgrade IOSNavigationStack

Conceptually:

@Composable
fun IOSNavigationStack(
    state: IOSNavigationState,
    transition: IOSBackTransitionState,
    modifier: Modifier = Modifier,
    content:
        @Composable (
            IOSNavigationEntry
        ) -> Unit
) {

    val savedStateHolder =
        rememberSaveableStateHolder()

    var widthPx by remember {
        mutableFloatStateOf(0f)
    }

    val progress =
        transition.progress.value

    val transforms =
        calculateIOSBackTransform(
            progress
        )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                widthPx =
                    size.width.toFloat()
            }
    ) {

        state.previous?.let { previous ->

            IOSNavigationScreen(
                entry = previous,
                stateHolder =
                    savedStateHolder,
                translationX =
                    widthPx *
                    transforms
                        .previousTranslationFraction,
                content = content
            )
        }

        IOSNavigationScreen(
            entry =
                state.current,

            stateHolder =
                savedStateHolder,

            translationX =
                widthPx *
                transforms
                    .currentTranslationFraction,

            content = content
        )
    }
}

Helper:

@Composable
private fun IOSNavigationScreen(
    entry: IOSNavigationEntry,
    stateHolder: SaveableStateHolder,
    translationX: Float,
    content:
        @Composable (
            IOSNavigationEntry
        ) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.translationX =
                    translationX
            }
    ) {

        stateHolder.SaveableStateProvider(
            key = entry.key
        ) {
            content(entry)
        }
    }
}

Notice we're using graphicsLayer for translation instead of triggering full layouts continuously.

That's important for high-refresh interactions.

12. Handle screen-width changes safely

Imagine:

1080px portrait

then rotation/tablet resize changes width.

Because we store:

progress = 0.4

instead of:

translation = 432px

we're safe.

New width:

1600px

becomes automatically:

0.4 × 1600
= 640px

This is another major benefit of normalized interaction state.

13. Add push transitions

So far:

tap Profile
→ Profile instantly appears

Let's fix that.

Create:

IOSPushTransitionState.kt
@Stable
class IOSPushTransitionState {

    val progress =
        Animatable(1f)

    suspend fun prepare() {

        progress.stop()

        progress.snapTo(0f)
    }

    suspend fun animate(
        spec: IOSSpringSpec =
            IOSMotionPreset.Smooth
    ) {

        progress.animateTo(
            targetValue = 1f,

            animationSpec =
                spring(
                    stiffness =
                        spec.stiffness,

                    dampingRatio =
                        spec.dampingRatio
                )
        )
    }

    suspend fun finish() {

        progress.stop()

        progress.snapTo(1f)
    }
}
14. Push transformation

For forward navigation:

Previous screen:
0 → slightly left

New screen:
100% right → 0

Create:

data class IOSPushTransform(
    val previousTranslationFraction: Float,
    val currentTranslationFraction: Float
)

Then:

fun calculateIOSPushTransform(
    progress: Float
): IOSPushTransform {

    val p =
        progress.coerceIn(
            0f,
            1f
        )

    return IOSPushTransform(

        previousTranslationFraction =
            -0.25f * p,

        currentTranslationFraction =
            1f - p
    )
}

Again, tuning values are ours.

15. Push sequence

When:

navigation.push(profile)

don't just add and render.

Do:

prepare progress = 0

add new entry

render:
previous + new

animate 0 → 1

transition complete

Conceptually:

suspend fun push(
    entry: IOSNavigationEntry
) {

    pushTransition.prepare()

    navigation.push(entry)

    pushTransition.animate()

    pushTransition.finish()
}

Now:

Home
        Profile enters →

actually feels related to the back gesture.

16. Make push/pop transformations symmetrical

This matters.

If push is:

Home shifts left
Profile enters from right

then pop should feel like reversing that physical relationship:

Profile follows finger right
Home returns from left

Not:

push = slide
pop = fade

unless a component intentionally requests a different transition.

Consistency contributes heavily to perceived polish.

17. Predictive Back cancellation

For system Predictive Back, cancellation isn't an error condition—we expect it.

Structure it:

PredictiveBackHandler(
    enabled =
        navigation.canGoBack
) { progressFlow ->

    try {

        controller.begin()

        progressFlow.collect {
            event ->

            transition.updateInteractive(
                progressValue =
                    event.progress,

                progressVelocity =
                    0f
            )
        }

        /*
         * Flow finishes normally:
         * system gesture committed.
         */

        transition.complete(
            initialVelocity = 0f
        )

        navigation.pop()

        transition.reset()

    } catch (
        cancellation:
            CancellationException
    ) {

        /*
         * User cancelled the system back gesture.
         */

        transition.cancel(
            initialVelocity =
                transition.velocity
        )
    }
}

The system progress is already normalized 0..1, so unlike our custom gesture we don't need to divide pixel distance.

18. Don't swallow coroutine cancellation

This is an important fix from some of our earlier prototype code.

Avoid:

catch (throwable: Throwable) {
    ...
}

around animation code unless you rethrow cancellation correctly.

Prefer:

catch (
    cancellation:
        CancellationException
) {

    phase =
        IOSBackTransitionPhase.Idle

    throw cancellation
}

Coroutine cancellation is normal control flow for interruptible animations.

We don't want the framework interpreting every interrupted animation as a genuine application error.

19. Add navigation events

Create:

sealed interface IOSNavigationEvent {

    data class Pushed(
        val entry: IOSNavigationEntry
    ) : IOSNavigationEvent

    data class Popped(
        val entry: IOSNavigationEntry
    ) : IOSNavigationEvent

    data object BackCancelled :
        IOSNavigationEvent
}

Eventually applications might observe:

navigation.events.collect { ... }

But don't implement a complicated event bus yet.

Keep this as a vocabulary for future work.

20. Test a real four-screen sequence

The Navigation Laboratory should now have:

Home
 ↓
Profile
 ↓
Post
 ↓
Comments

Do this test:

Home
→ Profile
→ Post
→ Comments

swipe back
→ Post

swipe back partially
→ cancel

still Post

swipe quickly
→ Profile

rotate device

still Profile

back
→ Home

Then verify state.

For example:

Profile LazyColumn:
item 43 visible

→ Post
→ swipe back

Profile should still show
item 43

That's why we added SaveableStateHolder.

21. State restoration test

Compose provides StateRestorationTester specifically for verifying saved-state behavior. Android's current state-saving documentation recommends actually testing restoration rather than assuming rememberSaveable works correctly for your architecture.

Eventually add a test conceptually like:

restorationTester.setContent {

    val navigation =
        rememberIOSNavigationState(
            home
        )

    TestNavigation(
        navigation
    )
}

Push:

Home → Profile → Post

Then:

restorationTester
    .emulateSavedInstanceStateRestore()

and verify:

current route == Post
stack size == 3
22. Navigation rendering architecture now

We have moved from the Phase 4A prototype:

two Composables
+
some translation

to:

              IOSNavigationState
                      │
                  Back stack
                      │
        ┌─────────────┴─────────────┐
        ↓                           ↓
 current entry                previous entry
        │                           │
        └─────────────┬─────────────┘
                      ↓
              SaveableStateHolder
                      ↓
                Screen renderer
                      ↓
              Transition progress
                 ↙          ↘
          push progress    pop progress
                 ↓            ↓
              graphicsLayer transforms

This is starting to resemble actual framework architecture.

23. Phase 4B tests we absolutely want

Add pure unit tests for:

✅ stack push
✅ stack pop
✅ cannot pop root
✅ duplicate keys rejected
✅ push transform at 0
✅ push transform at 1
✅ back transform at 0
✅ back transform at 1
✅ velocity normalization
✅ re-grab progress mapping

Especially re-grab.

Extract:

fun mapRegrabProgress(
    startProgress: Float,
    gestureProgress: Float
): Float {

    val start =
        startProgress.coerceIn(
            0f,
            1f
        )

    val gesture =
        gestureProgress.coerceIn(
            0f,
            1f
        )

    return start +
        gesture *
        (1f - start)
}

Test:

@Test
fun regrabDoesNotJumpBackToZero() {

    assertEquals(
        0.32f,
        mapRegrabProgress(
            startProgress = 0.32f,
            gestureProgress = 0f
        )
    )
}

And:

@Test
fun halfRemainingDragMapsCorrectly() {

    assertEquals(
        0.66f,
        mapRegrabProgress(
            startProgress = 0.32f,
            gestureProgress = 0.5f
        ),
        0.001f
    )
}

That tiny function prevents a very noticeable UX bug.

Phase 4B checkpoint

We should now have:

✅ multi-screen back stack
✅ interactive back
✅ animation interruption
✅ re-grab without position jump
✅ normalized animation velocity
✅ forward push animation
✅ symmetrical push/pop relationship
✅ resize-safe normalized progress
✅ predictive-back handling
✅ predictive-back cancellation
✅ SaveableStateHolder per screen
✅ navigation stack restoration
✅ preserved screen UI state
✅ coroutine cancellation handled correctly
✅ testable transformation mathematics
Phase 4 status

With 4A + 4B, Phase 4 is now complete enough to freeze the basic navigation architecture.

We're at:

Phase 0  Project foundation     ✅
Phase 1  Motion                ✅
Phase 2  Haptics               ✅
Phase 3  Gestures              ✅
Phase 4  Navigation            ✅

Phase 5  Scroll Physics         ← NEXT
Phase 6  Sheets
Phase 7  Materials / Glass
Phase 8  Components
Phase 9  Social-app benchmark