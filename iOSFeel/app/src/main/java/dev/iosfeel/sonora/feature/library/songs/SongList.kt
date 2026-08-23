package dev.iosfeel.sonora.feature.library.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.SongSort
import dev.iosfeel.sonora.core.model.SortDirection
import dev.iosfeel.sonora.core.model.sorted

@Composable
fun SongList(
    songs: List<Song>,
    sort: SongSort,
    sortDirection: SortDirection,
    onSortSelected: (SongSort) -> Unit,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val sortedSongs = songs.sorted(sort, sortDirection)

    IOSScrollableLazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${songs.size} Songs",
                    style = typography.subheadline,
                    color = colors.textSecondary
                )

                Row(
                    modifier = Modifier
                        .clickable {
                            val nextSort = when (sort) {
                                SongSort.Title -> SongSort.Artist
                                SongSort.Artist -> SongSort.Album
                                SongSort.Album -> SongSort.RecentlyAdded
                                SongSort.RecentlyAdded -> SongSort.Duration
                                SongSort.Duration -> SongSort.Title
                            }
                            onSortSelected(nextSort)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (sortDirection == SortDirection.Ascending) SonoraIcons.ArrowUp else SonoraIcons.ArrowDown,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = sort.name,
                        style = typography.caption1,
                        color = colors.accent
                    )
                }
            }
        }

        items(
            items = sortedSongs,
            key = { it.id },
            contentType = { "song" }
        ) { song ->
            SongRow(
                song = song,
                onClick = { onSongClick(song) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 80.dp),
                color = colors.separator.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
