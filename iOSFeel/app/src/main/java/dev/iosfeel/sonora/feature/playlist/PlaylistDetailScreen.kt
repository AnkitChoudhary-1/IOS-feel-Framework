package dev.iosfeel.sonora.feature.playlist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.floatingbar.IOSFloatingIconButton
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.sheet.SonoraActionItem
import dev.iosfeel.sonora.core.design.sheet.SonoraActionSheet
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
    var optionsSheetVisible by remember { mutableStateOf(false) }

    val playPressState = rememberIOSPressSurfaceState()
    val shufflePressState = rememberIOSPressSurfaceState()
    val addSongsPressState = rememberIOSPressSurfaceState()

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
        label = "playlist_title_pill_alpha"
    )

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
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accent)
                                    .iosPressSurface(
                                        state = playPressState,
                                        pressedScale = 0.95f,
                                        onClick = {
                                            haptics.impact(IOSImpact.Medium)
                                            onPlayAll(playlist.songs)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = SonoraIcons.Play,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Play",
                                        style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceElevated)
                                    .iosPressSurface(
                                        state = shufflePressState,
                                        pressedScale = 0.95f,
                                        onClick = {
                                            haptics.impact(IOSImpact.Medium)
                                            onShuffleAll(playlist.songs)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = SonoraIcons.Shuffle,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Shuffle",
                                        style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.accent
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.accent)
                                .iosPressSurface(
                                    state = addSongsPressState,
                                    pressedScale = 0.95f,
                                    onClick = onAddSongs
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = SonoraIcons.Plus,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add Songs",
                                    style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
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
                                text = "This playlist is empty. Tap 'Add Songs' to start building your mix.",
                                style = typography.subheadline,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
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

        // Modern iOS Blurred Floating Top Bar with Animated Title Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IOSFloatingIconButton(
                    onClick = onBack,
                    size = 40.dp
                ) {
                    Icon(
                        imageVector = SonoraIcons.ChevronLeft,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (titleAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .graphicsLayer {
                                alpha = titleAlpha
                                scaleX = 0.9f + (0.1f * titleAlpha)
                                scaleY = 0.9f + (0.1f * titleAlpha)
                            }
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = Color.Black.copy(alpha = 0.18f)
                            )
                            .clip(CircleShape)
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        IOSMaterialSurface(
                            config = IOSMaterialConfig(
                                style = IOSMaterialStyle.Regular,
                                cornerRadius = 20.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = SonoraIcons.Playlist,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = playlist.name,
                                    style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            IOSFloatingIconButton(
                onClick = { optionsSheetVisible = true },
                size = 40.dp
            ) {
                Icon(
                    imageVector = SonoraIcons.MoreHorizontal,
                    contentDescription = "Options",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // iOSFeel Action Sheet for Playlist Options
        SonoraActionSheet(
            visible = optionsSheetVisible,
            onDismiss = { optionsSheetVisible = false },
            title = playlist.name,
            subtitle = "${playlist.songs.size} songs",
            actions = listOf(
                SonoraActionItem(
                    title = "Add Songs",
                    icon = SonoraIcons.Plus,
                    onClick = onAddSongs
                ),
                SonoraActionItem(
                    title = "Rename Playlist",
                    icon = SonoraIcons.Edit,
                    onClick = onRenamePlaylist
                ),
                SonoraActionItem(
                    title = "Delete Playlist",
                    icon = SonoraIcons.Trash,
                    isDestructive = true,
                    onClick = onDeletePlaylist
                )
            )
        )
    }
}
