package dev.iosfeel.sonora.feature.library.artists

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.model.Artist

@Composable
fun ArtistList(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val sortedArtists = artists.sortedBy { it.name.lowercase() }

    IOSScrollableLazyColumn(
        modifier = modifier.fillMaxSize(),
        topFadeHeight = 20.dp,
        bottomFadeHeight = 88.dp
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "${artists.size} Artists",
                    style = typography.subheadline,
                    color = colors.textSecondary
                )
            }
        }

        items(
            items = sortedArtists,
            key = { it.id },
            contentType = { "artist" }
        ) { artist ->
            ArtistRow(
                artist = artist,
                onClick = { onArtistClick(artist) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 76.dp),
                color = colors.separator.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}
