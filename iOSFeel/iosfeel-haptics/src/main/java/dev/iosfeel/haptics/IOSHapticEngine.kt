package dev.iosfeel.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

class IOSHapticEngine(
    private val view: View,
    context: Context,
    private val policy: IOSHapticPolicy = IOSHapticPolicy()
) : IOSHaptics {

    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    val capabilities = detectHapticCapabilities(vibrator)

    private val rateLimiter = IOSHapticRateLimiter(
        minimumIntervalMs = policy.minimumIntervalMs
    )

    override fun selection() {
        perform(IOSHapticEvent.Selection)
    }

    override fun impact(strength: IOSImpact) {
        perform(IOSHapticEvent.Impact(strength))
    }

    override fun notification(type: IOSNotification) {
        perform(IOSHapticEvent.Notification(type))
    }

    override fun perform(event: IOSHapticEvent) {
        if (!policy.enabled) {
            return
        }

        if (!rateLimiter.shouldPerform(event)) {
            return
        }

        when (event) {
            IOSHapticEvent.Selection -> selectionInternal()
            IOSHapticEvent.GestureStart -> gestureStart()
            IOSHapticEvent.GestureEnd -> gestureEnd()
            IOSHapticEvent.ThresholdActivated -> thresholdActivated()
            IOSHapticEvent.ThresholdDeactivated -> thresholdDeactivated()
            is IOSHapticEvent.Impact -> impactInternal(event.strength)
            is IOSHapticEvent.Notification -> notificationInternal(event.type)
        }
    }

    private fun selectionInternal() {
        if (policy.preferSystemFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else {
            playPredefined(VibrationEffect.EFFECT_TICK)
        }
    }

    private fun gestureStart() {
        if (policy.preferSystemFeedback && Build.VERSION.SDK_INT >= 30) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
        } else {
            systemClick()
        }
    }

    private fun gestureEnd() {
        if (policy.preferSystemFeedback && Build.VERSION.SDK_INT >= 30) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
        } else {
            systemClick()
        }
    }

    private fun thresholdActivated() {
        if (policy.preferSystemFeedback && Build.VERSION.SDK_INT >= 34) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE)
        } else {
            selectionInternal()
        }
    }

    private fun thresholdDeactivated() {
        if (policy.preferSystemFeedback && Build.VERSION.SDK_INT >= 34) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE)
        } else {
            selectionInternal()
        }
    }

    private fun impactInternal(strength: IOSImpact) {
        val effect = when (strength) {
            IOSImpact.Light -> VibrationEffect.EFFECT_TICK
            IOSImpact.Medium -> VibrationEffect.EFFECT_CLICK
            IOSImpact.Heavy -> VibrationEffect.EFFECT_HEAVY_CLICK
        }
        playPredefined(effect)
    }

    private fun notificationInternal(type: IOSNotification) {
        when (type) {
            IOSNotification.Success -> {
                if (!richSuccess()) {
                    successSystem()
                }
            }
            IOSNotification.Warning -> {
                impactInternal(IOSImpact.Medium)
            }
            IOSNotification.Error -> {
                errorSystem()
            }
        }
    }

    private fun richSuccess(): Boolean {
        if (Build.VERSION.SDK_INT < 30 || !policy.allowRichEffects) {
            return false
        }

        val supported = vibrator.arePrimitivesSupported(
            VibrationEffect.Composition.PRIMITIVE_TICK,
            VibrationEffect.Composition.PRIMITIVE_CLICK
        )

        if (supported.any { !it }) {
            return false
        }

        val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.45f)
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.75f, 45)
            .compose()

        vibrator.vibrate(effect)
        return true
    }

    private fun successSystem() {
        if (policy.preferSystemFeedback && Build.VERSION.SDK_INT >= 30) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            playPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
        }
    }

    private fun errorSystem() {
        if (policy.preferSystemFeedback && Build.VERSION.SDK_INT >= 30) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            playPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        }
    }

    private fun systemClick() {
        if (policy.preferSystemFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } else {
            playPredefined(VibrationEffect.EFFECT_CLICK)
        }
    }

    private fun playPredefined(effectId: Int) {
        if (!capabilities.hasVibrator) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            fallbackClick()
        }
    }

    private fun fallbackClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(10L, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
}
