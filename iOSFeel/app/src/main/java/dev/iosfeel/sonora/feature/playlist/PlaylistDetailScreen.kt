package dev.iosfeel.sonora.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.feature.library.songs.SongRow

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onAddSongs: () -> Unit,
    onRenamePlaylist: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onSongOptionsClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (playlist == null) return

    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val listState = rememberLazyListState()
    val haptics = rememberIOSHaptics()
    var optionsMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        IOSScrollableLazyColumn(
            state = listState,
            topFadeHeight = 24.dp,
            bottomFadeHeight = 92.dp,
            contentPadding = PaddingValues(top = 70.dp, bottom = 140.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Big Mosaic Art, Name, Duration, Actions
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlaylistArtwork(
                        playlist = playlist,
                        cornerRadius = 20.dp,
                        modifier = Modifier.size(190.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = playlist.name,
                        style = typography.title1.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${playlist.songs.size} Songs • ${playlist.formattedDuration}",
                        style = typography.subheadline,
                        color = colors.textSecondary
                    )

                    if (playlist.songs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptics.impact(IOSImpact.Medium)
                                    onPlayAll(playlist.songs)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surfaceElevated,
                                    contentColor = colors.accent
                                )
                            ) {
                                Icon(
                                    imageVector = SonoraIcons.Play,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Play",
                                    style = typography.headline.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Button(
                                onClick = {
                                    haptics.impact(IOSImpact.Medium)
                                    onShuffleAll(playlist.songs)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surfaceElevated,
                                    contentColor = colors.accent
                                )
                            ) {
                                Icon(
                                    imageVector = SonoraIcons.Shuffle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Shuffle",
                                    style = typography.headline.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (playlist.songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "This playlist is empty",
                                style = typography.title3.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onAddSongs,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Icon(
                                    imageVector = SonoraIcons.Plus,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Songs", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(playlist.songs, key = { index, song -> "${song.id}_$index" }) { _, song ->
                    SongRow(
                        song = song,
                        onClick = { onSongClick(song, playlist.songs) },
                        onOptionsClick = { onSongOptionsClick(song) }
                    )
                }
            }
        }

        // Floating Top Bar Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceElevated.copy(alpha = 0.9f))
            ) {
                Icon(
                    imageVector = SonoraIcons.ChevronLeft,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { optionsMenuExpanded = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceElevated.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = SonoraIcons.MoreHorizontal,
                        contentDescription = "Options",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = optionsMenuExpanded,
                    onDismissRequest = { optionsMenuExpanded = false },
                    modifier = Modifier.background(colors.surfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Add Songs", color = colors.textPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = SonoraIcons.Plus,
                                contentDescription = null,
                                tint = colors.textPrimary
                            )
                        },
                        onClick = {
                            optionsMenuExpanded = false
                            onAddSongs()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Rename Playlist", color = colors.textPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = SonoraIcons.Edit,
                                contentDescription = null,
                                tint = colors.textPrimary
                            )
                        },
                        onClick = {
                            optionsMenuExpanded = false
                            onRenamePlaylist()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete Playlist", color = Color(0xFFFF3B30)) },
                        leadingIcon = {
                            Icon(
                                imageVector = SonoraIcons.Trash,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30)
                            )
                        },
                        onClick = {
                            optionsMenuExpanded = false
                            onDeletePlaylist()
                        }
                    )
                }
            }
        }
    }
}
