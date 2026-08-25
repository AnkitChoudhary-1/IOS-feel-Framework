package dev.iosfeel.sonora.feature.library.albums

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyVerticalGrid
import dev.iosfeel.sonora.core.model.Album

@Composable
fun AlbumGrid(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp)
) {
    IOSScrollableLazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        topFadeHeight = 16.dp,
        bottomFadeHeight = 90.dp
    ) {
        items(
            items = albums,
            key = { it.id },
            contentType = { "album" }
        ) { album ->
            AlbumItem(
                album = album,
                onClick = { onAlbumClick(album) }
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

