package dev.iosfeel.scroll.nested

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Generic coordinator for nested scroll handoffs between child lists and parent containers (e.g. Sheets).
 *
 * Implements deterministic delta consumption and full velocity transfer on boundary collisions.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSNestedScrollCoordinator(
    val onChildPreScroll: (available: Offset, source: NestedScrollSource) -> Offset = { _, _ -> Offset.Zero },
    val onChildPostScroll: (consumed: Offset, available: Offset, source: NestedScrollSource) -> Offset = { _, _, _ -> Offset.Zero },
    val onChildPreFling: suspend (available: Velocity) -> Velocity = { Velocity.Zero },
    val onChildPostFling: suspend (consumed: Velocity, available: Velocity) -> Velocity = { _, _ -> Velocity.Zero }
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        return onChildPreScroll(available, source)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        return onChildPostScroll(consumed, available, source)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return onChildPreFling(available)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        return onChildPostFling(consumed, available)
    }
}
