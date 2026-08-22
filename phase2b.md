Phase 2B starts with one architectural improvement: use Android’s semantic performHapticFeedback() path for ordinary UI interactions whenever possible, and reserve direct Vibrator effects/compositions for richer effects that genuinely need them. Android recommends action-based haptic feedback because it respects the user’s touch-feedback settings and lets the device choose appropriate hardware behavior.

1. Upgrade our architecture

Change the haptic pipeline from:

iOSFeel event
     ↓
Vibrator
     ↓
motor

to:

                 IOSHapticEvent
                       │
            ┌──────────┴──────────┐
            ↓                     ↓
      UI interaction        Rich/custom event
            ↓                     ↓
performHapticFeedback()     VibrationEffect
            ↓                     ↓
 Android/OEM tuning         capability check
            └──────────┬──────────┘
                       ↓
                   hardware

This is better because normal UI feedback automatically respects user/system touch-feedback preferences.

2. Add a haptic policy

Create:

IOSHapticPolicy.kt
package dev.iosfeel.haptics

data class IOSHapticPolicy(
    val enabled: Boolean = true,

    /**
     * Minimum gap between repeated identical
     * high-frequency events.
     */
    val minimumIntervalMs: Long = 35L,

    /**
     * Prefer Android semantic UI feedback
     * whenever an appropriate event exists.
     */
    val preferSystemFeedback: Boolean = true,

    /**
     * Allow richer vibrator compositions when
     * the hardware supports them.
     */
    val allowRichEffects: Boolean = true
)

This lets a developer eventually do:

IOSFeelTheme(
    haptics = IOSHapticPolicy(
        enabled = true,
        allowRichEffects = true
    )
) {
    App()
}
3. Make the engine aware of a View

For regular UI interactions we want:

view.performHapticFeedback(...)

Create a new engine constructor:

class IOSHapticEngine(
    private val view: View,
    context: Context,
    private val policy: IOSHapticPolicy =
        IOSHapticPolicy()
) : IOSHaptics {

Imports:

import android.view.HapticFeedbackConstants
import android.view.View

Why?

Android already has semantic events such as:

CONFIRM
REJECT
GESTURE_START
GESTURE_END
GESTURE_THRESHOLD_ACTIVATE
GESTURE_THRESHOLD_DEACTIVATE

The threshold events are almost exactly what we need later for sheets and navigation.

4. Expand our semantic vocabulary

Replace IOSHapticEvent.kt with:

package dev.iosfeel.haptics

sealed interface IOSHapticEvent {

    data object Selection : IOSHapticEvent

    data object GestureStart : IOSHapticEvent

    data object GestureEnd : IOSHapticEvent

    data object ThresholdActivated : IOSHapticEvent

    data object ThresholdDeactivated : IOSHapticEvent

    data class Impact(
        val strength: IOSImpact
    ) : IOSHapticEvent

    data class Notification(
        val type: IOSNotification
    ) : IOSHapticEvent
}

Now the framework can say:

haptics.perform(
    IOSHapticEvent.ThresholdActivated
)

rather than:

vibrateSomething()
5. Add rate limiting

Without protection, imagine a slider jumping around a threshold:

199
201  → tick
198
202  → tick
199
201  → tick

Within 50 milliseconds you could get:

tickticktickticktick

That's bad.

Create:

IOSHapticRateLimiter.kt
package dev.iosfeel.haptics

import android.os.SystemClock

internal class IOSHapticRateLimiter(
    private val minimumIntervalMs: Long
) {

    private var lastEventTime = Long.MIN_VALUE
    private var lastEventKey: Any? = null

    fun shouldPerform(
        key: Any
    ): Boolean {

        val now =
            SystemClock.elapsedRealtime()

        val elapsed =
            now - lastEventTime

        if (
            key == lastEventKey &&
            elapsed < minimumIntervalMs
        ) {
            return false
        }

        lastEventTime = now
        lastEventKey = key

        return true
    }

    fun reset() {
        lastEventTime = Long.MIN_VALUE
        lastEventKey = null
    }
}

We're rate-limiting the same event, rather than blocking every haptic globally.

So:

Selection
Selection 10ms later
→ blocked

but:

GestureStart
ThresholdActivated

can still represent different meaningful events.

6. Update the engine

Inside IOSHapticEngine:

private val rateLimiter =
    IOSHapticRateLimiter(
        minimumIntervalMs =
            policy.minimumIntervalMs
    )

Then the main entry point becomes:

override fun perform(
    event: IOSHapticEvent
) {

    if (!policy.enabled) {
        return
    }

    if (!rateLimiter.shouldPerform(event)) {
        return
    }

    when (event) {

        IOSHapticEvent.Selection ->
            selectionInternal()

        IOSHapticEvent.GestureStart ->
            gestureStart()

        IOSHapticEvent.GestureEnd ->
            gestureEnd()

        IOSHapticEvent.ThresholdActivated ->
            thresholdActivated()

        IOSHapticEvent.ThresholdDeactivated ->
            thresholdDeactivated()

        is IOSHapticEvent.Impact ->
            impactInternal(
                event.strength
            )

        is IOSHapticEvent.Notification ->
            notificationInternal(
                event.type
            )
    }
}

Then make the convenience functions delegate:

override fun selection() {
    perform(
        IOSHapticEvent.Selection
    )
}

override fun impact(
    strength: IOSImpact
) {
    perform(
        IOSHapticEvent.Impact(
            strength
        )
    )
}

override fun notification(
    type: IOSNotification
) {
    perform(
        IOSHapticEvent.Notification(
            type
        )
    )
}
7. Semantic system feedback

Now implement:

private fun gestureStart() {

    if (Build.VERSION.SDK_INT >= 30) {

        view.performHapticFeedback(
            HapticFeedbackConstants.GESTURE_START
        )

    } else {

        systemClick()
    }
}

And:

private fun gestureEnd() {

    if (Build.VERSION.SDK_INT >= 30) {

        view.performHapticFeedback(
            HapticFeedbackConstants.GESTURE_END
        )

    } else {

        systemClick()
    }
}
8. Threshold feedback

This is especially useful.

Modern Android has dedicated semantic constants specifically for a drag gesture crossing an actionable threshold.

Implement:

private fun thresholdActivated() {

    if (Build.VERSION.SDK_INT >= 34) {

        view.performHapticFeedback(
            HapticFeedbackConstants
                .GESTURE_THRESHOLD_ACTIVATE
        )

    } else {

        selectionInternal()
    }
}

And:

private fun thresholdDeactivated() {

    if (Build.VERSION.SDK_INT >= 34) {

        view.performHapticFeedback(
            HapticFeedbackConstants
                .GESTURE_THRESHOLD_DEACTIVATE
        )

    } else {

        selectionInternal()
    }
}

Conceptually:

dragging
       ↓

──────────────
snap threshold
──────────────
       ↓
ACTIVATE haptic

Then crossing backwards:

       ↑
DEACTIVATE haptic
──────────────

Very useful for:

pull-to-refresh
sheets
sliders
navigation decisions
detents
context menus
9. System-aware selection

Instead of sending EFFECT_TICK directly for every selection:

private fun selectionInternal() {

    view.performHapticFeedback(
        HapticFeedbackConstants
            .CLOCK_TICK
    )
}

Later we can refine which semantic constant is appropriate for each control.

The important difference is that performHapticFeedback() respects the user's touch-feedback settings, and doesn't require VIBRATE.

This means our library shouldn't try to override someone's decision to turn interface haptics off.

10. Confirm and reject

Implement success:

private fun successSystem() {

    if (Build.VERSION.SDK_INT >= 30) {

        view.performHapticFeedback(
            HapticFeedbackConstants.CONFIRM
        )

    } else {

        systemClick()
    }
}

Error:

private fun errorSystem() {

    if (Build.VERSION.SDK_INT >= 30) {

        view.performHapticFeedback(
            HapticFeedbackConstants.REJECT
        )

    } else {

        systemHeavyClick()
    }
}

Android defines CONFIRM and REJECT according to the meaning of the interaction rather than specifying an exact waveform.

11. Rich haptics

Now we add an advanced path.

Android 11/API 30 introduced VibrationEffect.Composition, which allows supported hardware primitives such as:

CLICK
TICK
LOW_TICK
THUD
QUICK_RISE
SLOW_RISE
QUICK_FALL
SPIN

But Android explicitly warns that composition primitives should be capability-checked because unsupported primitives do not automatically provide the same fallback behavior.

Therefore, never just assume composition support.

12. Expand capabilities

Update:

data class IOSHapticCapabilities(
    val hasVibrator: Boolean,

    val supportsTick: Boolean,
    val supportsClick: Boolean,
    val supportsHeavyClick: Boolean,

    val supportsPrimitiveClick: Boolean,
    val supportsPrimitiveTick: Boolean,
    val supportsPrimitiveLowTick: Boolean
)

For API 30+:

private fun primitiveSupported(
    vibrator: Vibrator,
    primitive: Int
): Boolean {

    if (Build.VERSION.SDK_INT < 30) {
        return false
    }

    return vibrator
        .arePrimitivesSupported(
            primitive
        )
        .firstOrNull()
        ?: false
}

Then detect:

supportsPrimitiveClick =
    primitiveSupported(
        vibrator,
        VibrationEffect.Composition
            .PRIMITIVE_CLICK
    )

and likewise for tick/low-tick.

13. Build our first composed effect

Let's create an iOSFeel success accent.

Do not claim this reproduces Apple's Taptic Engine.

It's simply our own interaction design.

private fun richSuccess(): Boolean {

    if (
        Build.VERSION.SDK_INT < 30 ||
        !policy.allowRichEffects
    ) {
        return false
    }

    val supported =
        vibrator.arePrimitivesSupported(
            VibrationEffect.Composition
                .PRIMITIVE_TICK,

            VibrationEffect.Composition
                .PRIMITIVE_CLICK
        )

    if (supported.any { !it }) {
        return false
    }

    val effect =
        VibrationEffect
            .startComposition()
            .addPrimitive(
                VibrationEffect.Composition
                    .PRIMITIVE_TICK,
                0.45f
            )
            .addPrimitive(
                VibrationEffect.Composition
                    .PRIMITIVE_CLICK,
                0.75f,
                45
            )
            .compose()

    vibrator.vibrate(effect)

    return true
}

Android's primitives support amplitude scaling from 0f to 1f, making it possible to create perceptibly different intensities without hardcoding raw motor amplitudes.

14. Success fallback chain

Then:

private fun notificationInternal(
    type: IOSNotification
) {

    when (type) {

        IOSNotification.Success -> {

            if (!richSuccess()) {
                successSystem()
            }
        }

        IOSNotification.Warning -> {

            impactInternal(
                IOSImpact.Medium
            )
        }

        IOSNotification.Error -> {

            errorSystem()
        }
    }
}

So:

Success requested
       ↓
Does rich hardware support it?
       │
      yes
       ↓
composition

otherwise:

       no
       ↓
Android CONFIRM
       ↓
OEM/system appropriate effect

That's proper progressive enhancement.

15. Build reusable threshold logic

The temporary HapticThresholdState from Phase 2A should become framework code.

Create:

IOSHapticThreshold.kt
package dev.iosfeel.haptics

class IOSHapticThreshold(
    private val threshold: Float
) {

    private var active = false

    fun update(
        value: Float,
        haptics: IOSHaptics
    ) {

        val newActive =
            value >= threshold

        if (
            newActive &&
            !active
        ) {

            haptics.perform(
                IOSHapticEvent
                    .ThresholdActivated
            )
        }

        if (
            !newActive &&
            active
        ) {

            haptics.perform(
                IOSHapticEvent
                    .ThresholdDeactivated
            )
        }

        active = newActive
    }

    fun reset() {
        active = false
    }
}

Then Motion Laboratory becomes:

val threshold =
    remember {
        IOSHapticThreshold(
            threshold = 200f
        )
    }

And:

LaunchedEffect(
    motionState.position.value
) {

    threshold.update(
        value =
            motionState.position.value,

        haptics =
            haptics
    )
}
16. But sheets require multiple thresholds

So let's immediately make the more useful version.

Create:

IOSHapticDetents.kt
package dev.iosfeel.haptics

class IOSHapticDetents(
    detents: List<Float>
) {

    private val detents =
        detents.sorted()

    private var currentIndex: Int? =
        null

    fun update(
        position: Float,
        haptics: IOSHaptics
    ) {

        val nearestIndex =
            detents
                .indices
                .minByOrNull { index ->

                    kotlin.math.abs(
                        detents[index] -
                            position
                    )
                }

        if (
            nearestIndex != null &&
            nearestIndex != currentIndex
        ) {

            if (currentIndex != null) {

                haptics.selection()
            }

            currentIndex =
                nearestIndex
        }
    }

    fun reset() {
        currentIndex = null
    }
}

Now imagine:

Sheet

100 px ───── compact
300 px ───── medium
600 px ───── large

As the closest detent changes:

Compact
    ↓
 *tick*
Medium
    ↓
 *tick*
Large

We're already preparing for Phase 6.

17. Fix rememberIOSHaptics

Because the engine now needs a real Android View:

@Composable
fun rememberIOSHaptics(
    policy: IOSHapticPolicy =
        IOSHapticPolicy()
): IOSHaptics {

    val context =
        LocalContext.current

    val view =
        LocalView.current

    return remember(
        context,
        view,
        policy
    ) {

        IOSHapticEngine(
            view = view,
            context =
                context.applicationContext,
            policy = policy
        )
    }
}

Imports:

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

Now iOSFeel's Compose integration directly participates in Android's normal haptic-feedback system.

18. Upgrade the laboratory

Make the Haptic Laboratory display:

iOSFeel Haptic Laboratory

System semantics
────────────────────────
[ Selection ]
[ Gesture Start ]
[ Gesture End ]
[ Threshold Activate ]
[ Threshold Deactivate ]

Impacts
────────────────────────
[ Light ] [ Medium ] [ Heavy ]

Notifications
────────────────────────
[ Success ]
[ Warning ]
[ Error ]

Rapid event test
────────────────────────
[ Spam Selection ]

Hardware
────────────────────────
Vibrator: YES
Tick primitive: YES
Click primitive: YES
Low tick primitive: NO
Rich effects: AVAILABLE

The Spam Selection button is useful.

Trigger selection rapidly several times and confirm the limiter prevents an unpleasant burst.

19. One very important correction to Phase 2A

Earlier we put:

<uses-permission
    android:name="android.permission.VIBRATE"
/>

Keep that permission only because our advanced custom VibrationEffect path uses the vibrator directly.

For:

View.performHapticFeedback()

you don't need that permission.

That's another reason the normal interaction path should prefer system semantic feedback.

20. Tests

Rate limiter testing becomes much easier if we inject time rather than directly calling SystemClock.

Improve it:

internal class IOSHapticRateLimiter(
    private val minimumIntervalMs: Long,
    private val clock: () -> Long = {
        SystemClock.elapsedRealtime()
    }
) {

    private var lastEventTime =
        Long.MIN_VALUE

    private var lastEventKey: Any? =
        null

    fun shouldPerform(
        key: Any
    ): Boolean {

        val now = clock()

        if (
            key == lastEventKey &&
            now - lastEventTime <
            minimumIntervalMs
        ) {
            return false
        }

        lastEventTime = now
        lastEventKey = key

        return true
    }
}

Test:

@Test
fun repeatedEventIsRateLimited() {

    var time = 1000L

    val limiter =
        IOSHapticRateLimiter(
            minimumIntervalMs = 40L,
            clock = { time }
        )

    assertTrue(
        limiter.shouldPerform("selection")
    )

    time += 10

    assertFalse(
        limiter.shouldPerform("selection")
    )

    time += 50

    assertTrue(
        limiter.shouldPerform("selection")
    )
}

And:

@Test
fun differentEventsAreNotBlocked() {

    var time = 1000L

    val limiter =
        IOSHapticRateLimiter(
            minimumIntervalMs = 40L,
            clock = { time }
        )

    assertTrue(
        limiter.shouldPerform("selection")
    )

    assertTrue(
        limiter.shouldPerform("threshold")
    )
}

This is why separating logic from Android hardware APIs matters.

Phase 2 status

We now have:

IOSHaptics
       │
       ├── semantic events
       │
       ├── policy
       │
       ├── rate limiter
       │
       ├── system preference aware
       │
       ├── View feedback
       │
       ├── predefined vibrator fallback
       │
       ├── rich composition capability
       │
       ├── threshold handling
       │
       └── detent handling
Phase 2 completion
✅ semantic haptic vocabulary
✅ selection / impacts
✅ success / warning / error
✅ gesture start/end
✅ threshold activate/deactivate
✅ respects Android touch-feedback settings
✅ rate limiting
✅ OEM/device-tuned system effects
✅ advanced composition support
✅ capability checks
✅ graceful fallback
✅ reusable threshold system
✅ reusable detent system
✅ Compose integration
✅ physical-device laboratory
✅ testable rate limiter

Phase 2 is now complete.

And the project has reached an important point:

Phase 1
Motion
   ↓
position + velocity + spring

        +

Phase 2
Haptics
   ↓
semantic tactile feedback