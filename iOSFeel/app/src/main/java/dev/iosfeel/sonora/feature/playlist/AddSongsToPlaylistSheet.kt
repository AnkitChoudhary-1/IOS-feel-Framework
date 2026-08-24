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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.search.IOSSearchField
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Song

@Composable
fun AddSongsToPlaylistSheet(
    allSongs: List<Song>,
    existingSongIds: Set<Long>,
    onAddSongs: (List<Long>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val haptics = rememberIOSHaptics()

    var searchQuery by remember { mutableStateOf("") }
    val selectedSongIds = remember { mutableStateListOf<Long>() }

    val filteredSongs = remember(searchQuery, allSongs, existingSongIds) {
        val available = allSongs.filter { !existingSongIds.contains(it.id) }
        if (searchQuery.isBlank()) available
        else {
            val q = searchQuery.trim().lowercase()
            available.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                (it.album?.lowercase()?.contains(q) == true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = colors.textSecondary, style = typography.headline)
                }

                Text(
                    text = "Add Songs",
                    style = typography.headline.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )

                Button(
                    onClick = {
                        haptics.impact(IOSImpact.Medium)
                        onAddSongs(selectedSongIds.toList())
                    },
                    enabled = selectedSongIds.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Done", color = Color.White, style = typography.headline)
                }
            }

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                IOSSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search songs to add",
                    containerColor = colors.surfaceSecondary,
                    textColor = colors.textPrimary,
                    placeholderColor = colors.textTertiary,
                    cancelTextColor = colors.accent,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Songs List
            IOSScrollableLazyColumn(
                contentPadding = PaddingValues(bottom = 60.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSongs, key = { it.id }) { song ->
                    val isSelected = selectedSongIds.contains(song.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptics.impact(IOSImpact.Light)
                                if (isSelected) {
                                    selectedSongIds.remove(song.id)
                                } else {
                                    selectedSongIds.add(song.id)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox Circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.accent else Color.Transparent)
                                .then(
                                    if (!isSelected) {
                                        Modifier.clip(CircleShape)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = SonoraIcons.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(colors.separator.copy(alpha = 0.4f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        SongArtwork(
                            song = song,
                            cornerRadius = 8.dp,
                            modifier = Modifier.size(46.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
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
                    }
                }
            }
        }
    }
}
