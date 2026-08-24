package dev.iosfeel.sonora.feature.library.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Song

import dev.iosfeel.components.iconbutton.IOSIconButton
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState

@Composable
fun SongRow(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOptionsClick: (() -> Unit)? = null
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val pressState = rememberIOSPressSurfaceState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .iosPressSurface(
                state = pressState,
                pressedScale = 0.98f,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongArtwork(
            song = song,
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = typography.body,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (!song.album.isNullOrBlank()) "${song.artist} • ${song.album}" else song.artist,
                style = typography.subheadline,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onOptionsClick != null) {
            IOSIconButton(
                onClick = onOptionsClick,
                size = 36.dp,
                contentDescription = "Song options"
            ) {
                Icon(
                    imageVector = SonoraIcons.MoreHorizontal,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
