package dev.iosfeel.sonora.feature.artist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.formatDuration

@Composable
fun ArtistSongs(
    songs: List<Song>,
    currentPlayingSongId: Long?,
    onSongClick: (Song, List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) return

    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Songs",
            style = typography.title2.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        songs.forEachIndexed { index, song ->
            val isPlaying = song.id == currentPlayingSongId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { onSongClick(song, songs) }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = SonoraIcons.MusicNote,
                        contentDescription = "Playing",
                        tint = colors.accent,
                        modifier = Modifier
                            .width(28.dp)
                            .size(16.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = typography.subheadline,
                        color = colors.textTertiary,
                        modifier = Modifier.width(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = typography.body.copy(
                            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isPlaying) colors.accent else colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    song.album?.let { albumName ->
                        Text(
                            text = albumName,
                            style = typography.caption1,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = song.durationMs.formatDuration(),
                    style = typography.caption1,
                    color = colors.textTertiary
                )
            }
        }
    }
}
