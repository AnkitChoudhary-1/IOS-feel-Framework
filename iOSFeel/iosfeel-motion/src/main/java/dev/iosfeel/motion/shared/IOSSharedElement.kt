package dev.iosfeel.motion.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import dev.iosfeel.motion.morph.IOSMorphBounds

/**
 * Composable marking content as a shared element that transforms continuously across screens.
 *
 * @param key Unique key matching source and destination elements.
 * @param isSource True if this is the source element (e.g. in list/grid), False if destination (e.g. details page).
 */
@Composable
fun IOSSharedElement(
    key: IOSSharedElementKey,
    modifier: Modifier = Modifier,
    isSource: Boolean = true,
    registry: IOSSharedElementRegistry = LocalIOSSharedElementRegistry.current,
    content: @Composable () -> Unit
) {
    DisposableEffect(key) {
        onDispose {
            registry.unregister(key)
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val pos = coordinates.positionInRoot()
            val size = coordinates.size
            val bounds = IOSMorphBounds(
                left = pos.x,
                top = pos.y,
                right = pos.x + size.width,
                bottom = pos.y + size.height
            )
            if (isSource) {
                registry.registerSource(key, bounds)
            } else {
                registry.registerTarget(key, bounds)
            }
        }
    ) {
        content()
    }
}
