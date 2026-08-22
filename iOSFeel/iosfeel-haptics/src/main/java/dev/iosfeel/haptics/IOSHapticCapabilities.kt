package dev.iosfeel.haptics

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

data class IOSHapticCapabilities(
    val hasVibrator: Boolean,

    val supportsTick: Boolean,
    val supportsClick: Boolean,
    val supportsHeavyClick: Boolean,

    val supportsPrimitiveClick: Boolean,
    val supportsPrimitiveTick: Boolean,
    val supportsPrimitiveLowTick: Boolean
)

internal fun detectHapticCapabilities(
    vibrator: Vibrator
): IOSHapticCapabilities {

    if (!vibrator.hasVibrator()) {
        return IOSHapticCapabilities(
            hasVibrator = false,
            supportsTick = false,
            supportsClick = false,
            supportsHeavyClick = false,
            supportsPrimitiveClick = false,
            supportsPrimitiveTick = false,
            supportsPrimitiveLowTick = false
        )
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        return IOSHapticCapabilities(
            hasVibrator = true,
            supportsTick = false,
            supportsClick = false,
            supportsHeavyClick = false,
            supportsPrimitiveClick = false,
            supportsPrimitiveTick = false,
            supportsPrimitiveLowTick = false
        )
    }

    fun supported(
        effect: Int
    ): Boolean {
        return vibrator.areAllEffectsSupported(
            effect
        ) == Vibrator.VIBRATION_EFFECT_SUPPORT_YES
    }

    fun primitiveSupported(
        primitive: Int
    ): Boolean {
        return vibrator.arePrimitivesSupported(
            primitive
        ).firstOrNull() ?: false
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
        ),

        supportsPrimitiveClick = primitiveSupported(
            VibrationEffect.Composition.PRIMITIVE_CLICK
        ),

        supportsPrimitiveTick = primitiveSupported(
            VibrationEffect.Composition.PRIMITIVE_TICK
        ),

        supportsPrimitiveLowTick = primitiveSupported(
            VibrationEffect.Composition.PRIMITIVE_LOW_TICK
        )
    )
}
