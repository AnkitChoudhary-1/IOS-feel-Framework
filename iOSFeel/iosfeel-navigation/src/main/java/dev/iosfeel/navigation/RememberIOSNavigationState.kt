package dev.iosfeel.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

internal val IOSNavigationStateSaver =
    Saver<IOSNavigationState, List<String>>(
        save = { state ->
            state.snapshotKeys()
        },
        restore = { values ->
            val entries = values.map { encoded ->
                val separator = encoded.indexOf('|')
                IOSNavigationEntry(
                    key = encoded.substring(0, separator),
                    route = encoded.substring(separator + 1)
                )
            }
            IOSNavigationState(
                initialEntries = entries
            )
        }
    )

@Composable
fun rememberIOSNavigationState(
    initialEntry: IOSNavigationEntry
): IOSNavigationState {
    return rememberSaveable(
        saver = IOSNavigationStateSaver
    ) {
        IOSNavigationState(
            initialEntries = listOf(initialEntry)
        )
    }
}
