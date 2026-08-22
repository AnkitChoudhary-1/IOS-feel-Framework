package dev.iosfeel.core

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Runtime device capabilities detected by the iOSFeel framework.
 *
 * Used to make rendering and behavior decisions at runtime.
 * Components should query these capabilities rather than hardcoding
 * assumptions about a specific device.
 *
 * @property apiLevel The device's Android API level
 * @property refreshRate The display's current refresh rate in Hz
 * @property supportsRenderEffect Whether RenderEffect (blur, etc.) is available (API 31+)
 * @property supportsRuntimeShader Whether AGSL RuntimeShader is available (API 33+)
 * @property reducedMotionEnabled Whether the user has requested reduced animations
 */
data class IOSFeelCapabilities(
    val apiLevel: Int,
    val refreshRate: Float,
    val supportsRenderEffect: Boolean,
    val supportsRuntimeShader: Boolean,
    val reducedMotionEnabled: Boolean
) {
    /**
     * Material rendering tier based on device capabilities.
     *
     * ```
     * High   → full material effects (blur, translucency, shaders)
     * Medium → simplified blur/translucency
     * Low    → solid tinted surfaces
     * ```
     */
    enum class MaterialTier {
        High,
        Medium,
        Low
    }

    /**
     * Determine the appropriate material rendering tier for this device.
     */
    val materialTier: MaterialTier
        get() = when {
            supportsRuntimeShader -> MaterialTier.High
            supportsRenderEffect -> MaterialTier.Medium
            else -> MaterialTier.Low
        }
}

/**
 * Remember and detect the current device's iOSFeel capabilities.
 *
 * This composable queries the device once and caches the result.
 * It detects API level, refresh rate, shader support, and
 * accessibility preferences.
 */
@Composable
fun rememberIOSFeelCapabilities(): IOSFeelCapabilities {
    val context = LocalContext.current
    return remember {
        val apiLevel = Build.VERSION.SDK_INT

        // Detect refresh rate
        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.refreshRate ?: 60f
        } else {
            @Suppress("DEPRECATION")
            val windowManager = context.getSystemService(WindowManager::class.java)
            windowManager?.defaultDisplay?.refreshRate ?: 60f
        }

        IOSFeelCapabilities(
            apiLevel = apiLevel,
            refreshRate = refreshRate,
            supportsRenderEffect = apiLevel >= Build.VERSION_CODES.S,
            supportsRuntimeShader = apiLevel >= Build.VERSION_CODES.TIRAMISU,
            reducedMotionEnabled = false // Detected separately via ReducedMotion.kt
        )
    }
}
