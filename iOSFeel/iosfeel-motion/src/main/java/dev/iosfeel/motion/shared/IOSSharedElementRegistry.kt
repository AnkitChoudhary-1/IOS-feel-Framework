package dev.iosfeel.motion.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.iosfeel.motion.morph.IOSMorphBounds

/**
 * Registry holding source and destination bounds of shared elements in transition coordinates.
 */
@Stable
class IOSSharedElementRegistry {
    private val sourceBoundsMap = mutableStateMapOf<IOSSharedElementKey, IOSMorphBounds>()
    private val targetBoundsMap = mutableStateMapOf<IOSSharedElementKey, IOSMorphBounds>()

    fun registerSource(key: IOSSharedElementKey, bounds: IOSMorphBounds) {
        sourceBoundsMap[key] = bounds
    }

    fun registerTarget(key: IOSSharedElementKey, bounds: IOSMorphBounds) {
        targetBoundsMap[key] = bounds
    }

    fun getSourceBounds(key: IOSSharedElementKey): IOSMorphBounds? = sourceBoundsMap[key]

    fun getTargetBounds(key: IOSSharedElementKey): IOSMorphBounds? = targetBoundsMap[key]

    fun unregister(key: IOSSharedElementKey) {
        sourceBoundsMap.remove(key)
        targetBoundsMap.remove(key)
    }

    fun clear() {
        sourceBoundsMap.clear()
        targetBoundsMap.clear()
    }
}

val LocalIOSSharedElementRegistry = staticCompositionLocalOf<IOSSharedElementRegistry> {
    IOSSharedElementRegistry()
}

/**
 * Provides a shared element registry to a Composable hierarchy.
 */
@Composable
fun IOSSharedElementProvider(
    registry: IOSSharedElementRegistry = remember { IOSSharedElementRegistry() },
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalIOSSharedElementRegistry provides registry) {
        content()
    }
}
