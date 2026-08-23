package dev.iosfeel.sonora.feature.library.artists

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.rememberIOSFlingBehavior
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.model.Artist

@Composable
fun ArtistList(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val colors = LocalSonoraColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        flingBehavior = rememberIOSFlingBehavior()
    ) {
        items(
            items = artists,
            key = { it.id },
            contentType = { "artist" }
        ) { artist ->
            ArtistRow(
                artist = artist,
                onClick = { onArtistClick(artist) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 78.dp),
                color = colors.separator.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
