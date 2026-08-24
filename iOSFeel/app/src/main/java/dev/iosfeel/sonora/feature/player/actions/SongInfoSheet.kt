package dev.iosfeel.sonora.feature.player.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Song

@Composable
fun SongInfoSheet(
    song: Song,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.textTertiary.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Song Artwork
        SongArtwork(
            song = song,
            cornerRadius = 16.dp,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = song.title,
            style = typography.title3.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )

        Text(
            text = song.artist,
            style = typography.subheadline,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Details Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceElevated)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoRow(label = "Album", value = song.album ?: "Unknown Album")
                InfoRow(label = "Artist", value = song.artist)
                InfoRow(label = "Duration", value = song.durationFormatted)
                if (song.year != null && song.year > 0) {
                    InfoRow(label = "Year", value = song.year.toString())
                }
                if (song.trackNumber != null && song.trackNumber > 0) {
                    InfoRow(label = "Track Number", value = song.trackNumber.toString())
                }
                if (song.contentUri != null) {
                    InfoRow(label = "Location", value = "Device Storage")
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = typography.subheadline,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = typography.body.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary
        )
    }
}
