package dev.iosfeel.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf

@Stable
class IOSNavigationState internal constructor(
    initialEntries: List<IOSNavigationEntry>
) {

    private val _entries = mutableStateListOf<IOSNavigationEntry>()
        .apply {
            addAll(initialEntries)
        }

    val entries: List<IOSNavigationEntry>
        get() = _entries

    val current: IOSNavigationEntry
        get() = _entries.last()

    val previous: IOSNavigationEntry?
        get() = _entries.getOrNull(_entries.lastIndex - 1)

    val canGoBack: Boolean
        get() = _entries.size > 1

    val size: Int
        get() = _entries.size

    fun push(
        entry: IOSNavigationEntry
    ) {
        require(
            _entries.none { it.key == entry.key }
        ) {
            "Navigation entry keys must be unique: ${entry.key}"
        }

        _entries.add(entry)
    }

    fun pop(): IOSNavigationEntry? {
        if (!canGoBack) {
            return null
        }

        return _entries.removeAt(_entries.lastIndex)
    }

    internal fun snapshotKeys(): List<String> {
        return _entries.map {
            "${it.key}|${it.route}"
        }
    }
}
