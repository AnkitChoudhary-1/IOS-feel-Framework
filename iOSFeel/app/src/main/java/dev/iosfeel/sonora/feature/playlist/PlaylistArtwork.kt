package dev.iosfeel.sonora.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song

@Composable
fun PlaylistArtwork(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    PlaylistArtworkFromSongs(
        songs = playlist.songs,
        modifier = modifier,
        cornerRadius = cornerRadius
    )
}

@Composable
fun PlaylistArtworkFromSongs(
    songs: List<Song>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val colors = LocalSonoraColors.current
    val distinctSongs = songs.distinctBy { it.albumId ?: it.id }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.surfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        when {
            distinctSongs.size >= 4 -> {
                // 2x2 Mosaic
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f)) {
                        SongArtwork(
                            song = distinctSongs[0],
                            cornerRadius = 0.dp,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                        SongArtwork(
                            song = distinctSongs[1],
                            cornerRadius = 0.dp,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        SongArtwork(
                            song = distinctSongs[2],
                            cornerRadius = 0.dp,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                        SongArtwork(
                            song = distinctSongs[3],
                            cornerRadius = 0.dp,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                    }
                }
            }
            distinctSongs.isNotEmpty() -> {
                // Single Artwork
                SongArtwork(
                    song = distinctSongs.first(),
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // Empty Playlist placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surfaceSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SonoraIcons.Playlist,
                        contentDescription = null,
                        tint = colors.accent.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
