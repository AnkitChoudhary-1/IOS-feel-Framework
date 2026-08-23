package dev.iosfeel.sonora.feature.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyRow
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.AlbumArtwork
import dev.iosfeel.sonora.core.model.Artist

@Composable
fun HomeArtistsSection(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    if (artists.isEmpty()) return

    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Artists",
            style = typography.title2.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        IOSScrollableLazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            startFadeWidth = 12.dp,
            endFadeWidth = 12.dp
        ) {
            items(artists, key = { it.id }) { artist ->
                val representativeAlbum = artist.albums.firstOrNull()

                Column(
                    modifier = Modifier
                        .width(96.dp)
                        .clickable { onArtistClick(artist) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (representativeAlbum != null) {
                        AlbumArtwork(
                            album = representativeAlbum,
                            cornerRadius = 48.dp,
                            modifier = Modifier.size(96.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = SonoraIcons.MusicNote,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = artist.name,
                        style = typography.subheadline.copy(fontWeight = FontWeight.Medium),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
