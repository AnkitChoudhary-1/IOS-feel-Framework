package dev.iosfeel.scroll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSScrollState(): IOSScrollState {
    return remember {
        IOSScrollState()
    }
}
