package dev.iosfeel.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberIOSHaptics(
    policy: IOSHapticPolicy = IOSHapticPolicy()
): IOSHaptics {

    val context = LocalContext.current
    val view = LocalView.current

    return remember(
        context,
        view,
        policy
    ) {
        IOSHapticEngine(
            view = view,
            context = context.applicationContext,
            policy = policy
        )
    }
}
