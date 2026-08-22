package dev.iosfeel.core

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Detect whether the user has enabled reduced motion / "Remove animations"
 * in Android system settings.
 *
 * When reduced motion is enabled, components should:
 * - Skip or shorten spring animations
 * - Reduce or eliminate decorative motion
 * - Use instant transitions where appropriate
 * - Still preserve functional animations that communicate state changes
 *
 * This checks both `ANIMATOR_DURATION_SCALE` and `TRANSITION_ANIMATION_SCALE`.
 * A value of 0 for either indicates the user wants reduced motion.
 */
fun isReducedMotionEnabled(context: Context): Boolean {
    return try {
        val animatorScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        val transitionScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        animatorScale == 0f || transitionScale == 0f
    } catch (_: Exception) {
        false
    }
}

/**
 * Remember the current reduced motion preference.
 *
 * Note: This value is read once at composition time. If the user changes
 * their animation settings while the app is in the foreground, a
 * recomposition would be needed to reflect the change.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        isReducedMotionEnabled(context)
    }
}
