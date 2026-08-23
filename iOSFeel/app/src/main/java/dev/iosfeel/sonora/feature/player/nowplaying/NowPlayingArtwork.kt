package dev.iosfeel.sonora.feature.player.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.artwork.MissingArtwork
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Song

@Composable
fun NowPlayingArtwork(
    song: Song?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (song != null) {
                SongArtwork(
                    song = song,
                    cornerRadius = 16.dp,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                MissingArtwork(
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 16.dp,
                    iconSize = 64.dp
                )
            }
        }
    }
}
