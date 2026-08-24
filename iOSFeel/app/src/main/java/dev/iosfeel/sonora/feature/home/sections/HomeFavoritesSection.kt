package dev.iosfeel.sonora.feature.home.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyRow
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Song

@Composable
fun HomeFavoritesSection(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) return

    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorites",
                style = typography.title2.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "See All",
                style = typography.subheadline.copy(color = colors.accent),
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        IOSScrollableLazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            startFadeWidth = 16.dp,
            endFadeWidth = 16.dp
        ) {
            items(songs, key = { it.id }) { song ->
                Column(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onSongClick(song, songs) }
                ) {
                    SongArtwork(
                        song = song,
                        cornerRadius = 12.dp,
                        modifier = Modifier.size(130.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = song.title,
                        style = typography.body.copy(fontWeight = FontWeight.SemiBold),
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
            }
        }
    }
}
