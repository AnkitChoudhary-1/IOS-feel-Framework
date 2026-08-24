package dev.iosfeel.sonora.feature.player.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Song

data class SongActionContext(
    val playlistId: Long? = null,
    val allowRemoveFromPlaylist: Boolean = false
)

@Composable
fun SongActionsSheet(
    song: Song,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onGoToAlbum: (() -> Unit)? = null,
    onGoToArtist: (() -> Unit)? = null,
    onSongInfo: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    context: SongActionContext = SongActionContext(),
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val haptics = rememberIOSHaptics()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.textTertiary.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Song Header Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongArtwork(
                song = song,
                cornerRadius = 10.dp,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = typography.headline,
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
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Actions Group 1: Playback Queue
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceElevated)
        ) {
            Column {
                ActionRow(
                    icon = SonoraIcons.SkipNext,
                    title = "Play Next",
                    onClick = {
                        haptics.impact(IOSImpact.Light)
                        onPlayNext()
                    }
                )
                ActionDivider()
                ActionRow(
                    icon = SonoraIcons.MusicNote,
                    title = "Add to Queue",
                    onClick = {
                        haptics.impact(IOSImpact.Light)
                        onAddToQueue()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Group 2: Library, Playlists & Favorites
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceElevated)
        ) {
            Column {
                ActionRow(
                    icon = if (isFavorite) SonoraIcons.HeartFilled else SonoraIcons.Heart,
                    title = if (isFavorite) "Remove from Favorites" else "Favorite",
                    iconTint = if (isFavorite) Color(0xFFFF2D55) else colors.textPrimary,
                    onClick = {
                        haptics.impact(IOSImpact.Medium)
                        onToggleFavorite()
                    }
                )
                ActionDivider()
                ActionRow(
                    icon = SonoraIcons.Playlist,
                    title = "Add to a Playlist...",
                    onClick = {
                        haptics.impact(IOSImpact.Light)
                        onAddToPlaylist()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Group 3: Navigation & Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceElevated)
        ) {
            Column {
                if (onGoToAlbum != null) {
                    ActionRow(
                        icon = SonoraIcons.Folder,
                        title = "Go to Album",
                        onClick = {
                            haptics.impact(IOSImpact.Light)
                            onGoToAlbum()
                        }
                    )
                    ActionDivider()
                }

                if (onGoToArtist != null) {
                    ActionRow(
                        icon = SonoraIcons.Folder,
                        title = "Go to Artist",
                        onClick = {
                            haptics.impact(IOSImpact.Light)
                            onGoToArtist()
                        }
                    )
                    ActionDivider()
                }

                ActionRow(
                    icon = SonoraIcons.Info,
                    title = "Song Info",
                    onClick = {
                        haptics.impact(IOSImpact.Light)
                        onSongInfo()
                    }
                )

                if (context.allowRemoveFromPlaylist && onRemoveFromPlaylist != null) {
                    ActionDivider()
                    ActionRow(
                        icon = SonoraIcons.Trash,
                        title = "Remove from Playlist",
                        iconTint = Color(0xFFFF3B30),
                        textColor = Color(0xFFFF3B30),
                        onClick = {
                            haptics.impact(IOSImpact.Medium)
                            onRemoveFromPlaylist()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = LocalSonoraColors.current.textPrimary,
    textColor: Color = LocalSonoraColors.current.textPrimary
) {
    val typography = LocalSonoraTypography.current
    val pressState = rememberIOSPressSurfaceState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .iosPressSurface(
                state = pressState,
                pressedScale = 0.97f,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = typography.body.copy(fontWeight = FontWeight.Normal),
            color = textColor
        )
    }
}

@Composable
private fun ActionDivider() {
    val colors = LocalSonoraColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 52.dp)
            .height(0.5.dp)
            .background(colors.separator.copy(alpha = 0.3f))
    )
}
