package dev.iosfeel.sonora.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.scroll.rememberIOSFlingBehavior
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.artwork.AlbumArtwork
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.stats

@Composable
fun HomeScreen(
    library: MusicLibrary = MusicLibrary.Empty,
    onAlbumClick: (Album) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val stats = library.stats()
    val recentAlbums = library.albums.take(8)
    val recentSongs = library.songs.sortedByDescending { it.dateAddedSeconds }.take(10)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        flingBehavior = rememberIOSFlingBehavior()
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(54.dp))

                Text(
                    text = "Listen Now",
                    style = typography.largeTitle.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Library Overview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceElevated)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "LOCAL LIBRARY",
                            style = typography.caption1.copy(
                                color = colors.accent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (stats.songs > 0) "${stats.songs} Songs Available" else "Your Offline Collection",
                            style = typography.title2.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (stats.songs > 0)
                                "${stats.albums} Albums • ${stats.artists} Artists"
                            else
                                "Open the Library tab to scan local audio files.",
                            style = typography.body.copy(
                                color = colors.textSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (recentAlbums.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Recently Added Albums",
                        style = typography.title2.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        flingBehavior = rememberIOSFlingBehavior()
                    ) {
                        items(recentAlbums, key = { it.id }) { album ->
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { onAlbumClick(album) }
                            ) {
                                AlbumArtwork(
                                    album = album,
                                    cornerRadius = 10.dp,
                                    modifier = Modifier.size(140.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = album.title,
                                    style = typography.headline,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = album.artist,
                                    style = typography.subheadline,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        if (recentSongs.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Newest Tracks",
                        style = typography.title2.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(recentSongs, key = { it.id }) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(song) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = typography.body,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = typography.subheadline,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = song.durationFormatted,
                        style = typography.caption1,
                        color = colors.textTertiary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
