package dev.iosfeel.sonora.feature.artist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyRow
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.artwork.AlbumArtwork
import dev.iosfeel.sonora.core.model.Album

@Composable
fun ArtistAlbums(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    if (albums.isEmpty()) return

    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Albums",
            style = typography.title2.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        IOSScrollableLazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            startFadeWidth = 16.dp,
            endFadeWidth = 16.dp
        ) {
            items(albums, key = { it.id }) { album ->
                Column(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onAlbumClick(album) }
                ) {
                    AlbumArtwork(
                        album = album,
                        cornerRadius = 10.dp,
                        modifier = Modifier.size(130.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = album.title,
                        style = typography.subheadline.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    album.year?.let { year ->
                        Text(
                            text = "$year",
                            style = typography.caption1,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
