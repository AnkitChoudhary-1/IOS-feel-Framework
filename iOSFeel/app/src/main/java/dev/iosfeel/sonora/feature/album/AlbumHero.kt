package dev.iosfeel.sonora.feature.album

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.AlbumArtwork
import dev.iosfeel.sonora.core.model.Album

@Composable
fun AlbumHero(
    album: Album,
    onArtistClick: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AlbumArtwork(
            album = album,
            cornerRadius = 16.dp,
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = album.title,
            style = typography.title1.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = album.artist,
            style = typography.title3,
            color = colors.accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(onClick = onArtistClick)
        )

        Spacer(modifier = Modifier.height(4.dp))

        val metaString = buildString {
            if (album.year != null && album.year > 0) {
                append("${album.year} · ")
            }
            append("${album.songCount} songs")
        }

        Text(
            text = metaString,
            style = typography.subheadline,
            color = colors.textTertiary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Icon(
                    imageVector = SonoraIcons.Play,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Play",
                    style = typography.headline
                )
            }

            FilledTonalButton(
                onClick = onShuffleAll,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.surfaceElevated,
                    contentColor = colors.accent
                )
            ) {
                Icon(
                    imageVector = SonoraIcons.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Shuffle",
                    style = typography.headline
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
