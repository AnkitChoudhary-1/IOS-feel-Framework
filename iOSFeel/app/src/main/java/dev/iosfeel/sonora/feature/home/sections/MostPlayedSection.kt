package dev.iosfeel.sonora.feature.home.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.formatDuration

@Composable
fun MostPlayedSection(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) return

    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Most Played",
            style = typography.title2.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        songs.take(10).forEachIndexed { index, song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { onSongClick(song, songs) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayNumber = String.format("%02d", index + 1)
                Text(
                    text = displayNumber,
                    style = typography.subheadline.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.accent,
                    modifier = Modifier.width(32.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = typography.body.copy(fontWeight = FontWeight.Medium),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = song.artist,
                        style = typography.caption1,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
