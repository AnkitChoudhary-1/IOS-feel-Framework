We are now starting Phase 2: Haptic Engine.

The important design decision is that iOSFeel should not expose vibration durations like vibrate(12ms). Components should request meaning:

haptics.selection()
haptics.impact(IOSImpact.Light)
haptics.success()

Then the engine chooses the best Android implementation for the device.

Android already provides device-tuned predefined effects such as TICK, CLICK, HEAVY_CLICK, and DOUBLE_CLICK; Android specifically notes that these may be tailored to the hardware, which is better for us than inventing fixed vibration waveforms.

Phase 2 structure

Add:

iosfeel-haptics/
└── src/main/java/dev/iosfeel/haptics/
    ├── IOSHaptics.kt
    ├── IOSHapticEngine.kt
    ├── IOSHapticEvent.kt
    ├── IOSImpact.kt
    ├── IOSNotification.kt
    ├── IOSHapticCapabilities.kt
    └── RememberIOSHaptics.kt

And change the dependency direction to:

              app
               │
        ┌──────┴──────┐
        ↓             ↓
 iosfeel-motion   iosfeel-haptics
        │             │
        └──────┬──────┘
               ↓
         iosfeel-core

Haptics should not depend on motion.

Later:

IOSSheet
   ↓
motion + haptics

combines them.

1. Create the module

Create a new Android Library:

Module:
iosfeel-haptics

Package:
dev.iosfeel.haptics

Then add it to the sample app:

dependencies {
    implementation(project(":iosfeel-core"))
    implementation(project(":iosfeel-motion"))
    implementation(project(":iosfeel-haptics"))
}

Also put this in the app manifest:

<uses-permission android:name="android.permission.VIBRATE" />

VibratorManager is available from API 31 and gives access to the device's default vibrator; because our minimum SDK is lower, we'll use Vibrator as the fallback.

2. Define semantic impacts

Create IOSImpact.kt:

package dev.iosfeel.haptics

enum class IOSImpact {
    Light,
    Medium,
    Heavy
}

Then IOSNotification.kt:

package dev.iosfeel.haptics

enum class IOSNotification {
    Success,
    Warning,
    Error
}

Notice we're not calling these things:

5msVibration
10msVibration
180AmplitudeVibration

The component shouldn't care about hardware implementation.

3. Define semantic events

Create IOSHapticEvent.kt:

package dev.iosfeel.haptics

sealed interface IOSHapticEvent {

    data object Selection : IOSHapticEvent

    data class Impact(
        val strength: IOSImpact
    ) : IOSHapticEvent

    data class Notification(
        val type: IOSNotification
    ) : IOSHapticEvent
}

This gives us a central vocabulary:

IOSHapticEvent
│
├── Selection
│
├── Impact
│   ├── Light
│   ├── Medium
│   └── Heavy
│
└── Notification
    ├── Success
    ├── Warning
    └── Error
4. Define the public API

Create IOSHaptics.kt:

package dev.iosfeel.haptics

interface IOSHaptics {

    fun selection()

    fun impact(
        strength: IOSImpact
    )

    fun notification(
        type: IOSNotification
    )

    fun perform(
        event: IOSHapticEvent
    )
}

This is what future components interact with.

For example:

haptics.selection()

or:

haptics.impact(
    IOSImpact.Light
)
5. Detect device capabilities

Create IOSHapticCapabilities.kt:

package dev.iosfeel.haptics

data class IOSHapticCapabilities(
    val hasVibrator: Boolean,
    val supportsTick: Boolean,
    val supportsClick: Boolean,
    val supportsHeavyClick: Boolean
)

Now create a capability detector:

package dev.iosfeel.haptics

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

internal fun detectHapticCapabilities(
    vibrator: Vibrator
): IOSHapticCapabilities {

    if (!vibrator.hasVibrator()) {
        return IOSHapticCapabilities(
            hasVibrator = false,
            supportsTick = false,
            supportsClick = false,
            supportsHeavyClick = false
        )
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        return IOSHapticCapabilities(
            hasVibrator = true,
            supportsTick = false,
            supportsClick = false,
            supportsHeavyClick = false
        )
    }

    fun supported(
        effect: Int
    ): Boolean {

        return vibrator.areAllEffectsSupported(
            effect
        ) == Vibrator.VIBRATION_EFFECT_SUPPORT_YES
    }

    return IOSHapticCapabilities(
        hasVibrator = true,

        supportsTick = supported(
            VibrationEffect.EFFECT_TICK
        ),

        supportsClick = supported(
            VibrationEffect.EFFECT_CLICK
        ),

        supportsHeavyClick = supported(
            VibrationEffect.EFFECT_HEAVY_CLICK
        )
    )
}

Android can report whether predefined effects are supported natively; unsupported effects may still receive system fallback behavior.

6. Build the actual engine

Create IOSHapticEngine.kt.

package dev.iosfeel.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class IOSHapticEngine(
    context: Context
) : IOSHaptics {

    private val vibrator: Vibrator =
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {

            val manager =
                context.getSystemService(
                    VibratorManager::class.java
                )

            manager.defaultVibrator

        } else {

            @Suppress("DEPRECATION")
            context.getSystemService(
                Context.VIBRATOR_SERVICE
            ) as Vibrator
        }

    val capabilities =
        detectHapticCapabilities(
            vibrator
        )

    override fun selection() {
        playPredefined(
            VibrationEffect.EFFECT_TICK
        )
    }

    override fun impact(
        strength: IOSImpact
    ) {

        val effect =
            when (strength) {

                IOSImpact.Light ->
                    VibrationEffect.EFFECT_TICK

                IOSImpact.Medium ->
                    VibrationEffect.EFFECT_CLICK

                IOSImpact.Heavy ->
                    VibrationEffect.EFFECT_HEAVY_CLICK
            }

        playPredefined(effect)
    }

    override fun notification(
        type: IOSNotification
    ) {

        when (type) {

            IOSNotification.Success ->
                success()

            IOSNotification.Warning ->
                warning()

            IOSNotification.Error ->
                error()
        }
    }

    override fun perform(
        event: IOSHapticEvent
    ) {

        when (event) {

            IOSHapticEvent.Selection ->
                selection()

            is IOSHapticEvent.Impact ->
                impact(
                    event.strength
                )

            is IOSHapticEvent.Notification ->
                notification(
                    event.type
                )
        }
    }

    private fun playPredefined(
        effectId: Int
    ) {

        if (!capabilities.hasVibrator) {
            return
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {

            vibrator.vibrate(
                VibrationEffect.createPredefined(
                    effectId
                )
            )

        } else {

            fallbackClick()
        }
    }

    private fun fallbackClick() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    10L,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    private fun success() {

        /*
         * Temporary Phase-2 implementation.
         * We'll improve semantic compositions later.
         */

        playPredefined(
            VibrationEffect.EFFECT_DOUBLE_CLICK
        )
    }

    private fun warning() {

        playPredefined(
            VibrationEffect.EFFECT_CLICK
        )
    }

    private fun error() {

        playPredefined(
            VibrationEffect.EFFECT_HEAVY_CLICK
        )
    }
}

Predefined effects were introduced in API 29, while VibrationEffect itself goes back to API 26.

7. One thing we're deliberately avoiding

Don't do this everywhere:

VibrationEffect.createOneShot(
    20,
    255
)

because:

Phone A
→ powerful actuator

Phone B
→ weak motor

Phone C
→ different resonant characteristics

The exact same waveform can feel completely different.

Instead:

semantic request

"give me a click"
        ↓
Android predefined effect
        ↓
device-specific implementation
        ↓
actual actuator

Android explicitly says predefined effects can be hardware-tailored, which is exactly what we want.

8. Compose API

Now create RememberIOSHaptics.kt:

package dev.iosfeel.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberIOSHaptics(): IOSHaptics {

    val context =
        LocalContext.current.applicationContext

    return remember(context) {
        IOSHapticEngine(context)
    }
}

Now any Compose screen can simply do:

val haptics =
    rememberIOSHaptics()

and:

haptics.selection()
9. Add a Haptic Laboratory

Your laboratory home can now enable:

Motion      ✅
Haptics     ✅
Gestures    soon
Navigation  soon
Scrolling   soon
Materials   soon
Sheets      soon

Create:

HapticLaboratory.kt

with something like:

@Composable
fun HapticLaboratory() {

    val haptics =
        rememberIOSHaptics()

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "iOSFeel Haptic Laboratory"
        )

        Button(
            onClick = {
                haptics.selection()
            }
        ) {
            Text("Selection")
        }

        Button(
            onClick = {
                haptics.impact(
                    IOSImpact.Light
                )
            }
        ) {
            Text("Light impact")
        }

        Button(
            onClick = {
                haptics.impact(
                    IOSImpact.Medium
                )
            }
        ) {
            Text("Medium impact")
        }

        Button(
            onClick = {
                haptics.impact(
                    IOSImpact.Heavy
                )
            }
        ) {
            Text("Heavy impact")
        }

        Button(
            onClick = {
                haptics.notification(
                    IOSNotification.Success
                )
            }
        ) {
            Text("Success")
        }
    }
}

Run this on a physical phone.

Emulators are not useful for judging actuator feel.

10. Now connect Phase 1 and Phase 2

This is where the project starts getting interesting.

Return to our motion card.

Suppose we establish a threshold:

                 threshold
                     │
─────────────────────│────────→
                     │
                    180px

As you drag:

0 → 50 → 100 → 150 → 179

nothing happens.

Then:

179
 ↓
182

we trigger:

haptics.selection()

exactly once.

Not repeatedly while remaining past the threshold.

11. Create threshold state

Create in the sample application first:

class HapticThresholdState {

    var crossed by mutableStateOf(false)
        private set

    fun update(
        value: Float,
        threshold: Float,
        onCross: () -> Unit
    ) {

        val nowCrossed =
            value >= threshold

        if (
            nowCrossed &&
            !crossed
        ) {
            onCross()
        }

        crossed = nowCrossed
    }

    fun reset() {
        crossed = false
    }
}

Eventually we'll move a generalized version into the framework.

12. Why the threshold matters

Imagine an iOS-like sheet with:

Compact
Medium
Large

When you're dragging:

Compact
   ↓

----- medium threshold -----

   ↓ *tiny click*

Medium

That small tactile event makes the interface feel like the sheet has entered a physical notch.

Later:

IOSSheet
       │
drag position
       ↓
detent crossed
       ↓
IOSHaptics.selection()
       ↓
spring → detent

That's far more interesting than vibrating when a button is clicked.

13. Add a haptic threshold to the motion card

Inside MotionLaboratory:

val haptics =
    rememberIOSHaptics()

var thresholdCrossed by remember {
    mutableStateOf(false)
}

val threshold = 200f

Monitor position:

LaunchedEffect(
    motionState.position.value
) {

    val crossed =
        motionState.position.value >=
            threshold

    if (
        crossed &&
        !thresholdCrossed
    ) {

        haptics.selection()
    }

    thresholdCrossed =
        crossed
}

Then test:

CARD →

100 px
150 px
190 px

200 px
   ↑
 tiny tick

Move backwards:

200
190
180

then cross again:

190
201
 ↑
tick again

This is already starting to behave like a tactile control.

14. Important principle

We should avoid this:

dragging

*bzz*
*bzz*
*bzz*
*bzz*
*bzz*

That feels terrible.

Instead:

continuous visual interaction
              ↓
 meaningful state boundary
              ↓
         tiny feedback

Think of haptics as punctuation.

Not background noise.

15. Phase 2A status

At this point we have:

iosfeel-haptics

IOSHaptics
     │
     ├── selection()
     │
     ├── impact()
     │
     ├── notification()
     │
     └── perform()
           ↓
   IOSHapticEngine
           ↓
   capability detection
           ↓
 Android VibrationEffect
           ↓
 device actuator

And:

Motion Engine
      +
Haptic Engine
      ↓
threshold interaction
Completed so far
✅ Semantic haptic API
✅ Selection feedback
✅ Light impact
✅ Medium impact
✅ Heavy impact
✅ Success/warning/error vocabulary
✅ VibratorManager support
✅ Pre-API-31 fallback
✅ Device capability detection
✅ Compose remember API
✅ Haptic Laboratory
✅ Motion threshold feedback