package dev.iosfeel.sonora.core.model

enum class SongSort {
    Title,
    Artist,
    Album,
    RecentlyAdded,
    Duration
}

enum class SortDirection {
    Ascending,
    Descending
}

fun List<Song>.sorted(
    sort: SongSort,
    direction: SortDirection = SortDirection.Ascending
): List<Song> {
    val result = when (sort) {
        SongSort.Title -> sortedBy { it.title.lowercase() }
        SongSort.Artist -> sortedBy { it.artist.lowercase() }
        SongSort.Album -> sortedBy { it.album?.lowercase().orEmpty() }
        SongSort.RecentlyAdded -> sortedBy { it.dateAddedSeconds }
        SongSort.Duration -> sortedBy { it.durationMs }
    }

    return if (direction == SortDirection.Ascending) {
        result
    } else {
        result.reversed()
    }
}

enum class LibrarySection {
    Songs,
    Albums,
    Artists
}
