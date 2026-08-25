package dev.iosfeel.sonora.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.Playlist

import dev.iosfeel.components.iconbutton.IOSIconButton
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import dev.iosfeel.components.floatingbar.IOSFloatingIconButton
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.scroll.IOSScrollableLazyVerticalGrid

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onBack: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val gridState = rememberLazyGridState()
    val haptics = rememberIOSHaptics()
    val newPlaylistPressState = rememberIOSPressSurfaceState()

    val isScrolled by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 40
        }
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
        label = "playlists_title_pill_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        IOSScrollableLazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            topFadeHeight = 24.dp,
            bottomFadeHeight = 140.dp,
            contentPadding = PaddingValues(top = 80.dp, bottom = 140.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header span
            item(span = { GridItemSpan(2) }) {
                Column {
                    Spacer(modifier = Modifier.statusBarsPadding())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlists",
                            style = typography.largeTitle.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )

                        IOSIconButton(
                            onClick = onCreatePlaylist,
                            size = 36.dp,
                            contentDescription = "New Playlist"
                        ) {
                            Icon(
                                imageVector = SonoraIcons.Plus,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // New Playlist Card Action
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceElevated)
                        .iosPressSurface(
                            state = newPlaylistPressState,
                            pressedScale = 0.95f,
                            onClick = onCreatePlaylist
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = SonoraIcons.Plus,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "New Playlist",
                            style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.accent
                        )
                    }
                }
            }

            // Playlists items
            items(playlists, key = { it.id }) { playlist ->
                PlaylistItem(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) }
                )
            }

            if (playlists.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Create custom playlists to organize your favorite tracks.",
                                style = typography.subheadline,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
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
                                    text = "Playlists",
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
                onClick = onCreatePlaylist,
                size = 40.dp
            ) {
                Icon(
                    imageVector = SonoraIcons.Plus,
                    contentDescription = "New Playlist",
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val pressState = rememberIOSPressSurfaceState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .iosPressSurface(
                state = pressState,
                pressedScale = 0.96f,
                onClick = onClick
            )
    ) {
        PlaylistArtwork(
            playlist = playlist,
            cornerRadius = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = playlist.name,
            style = typography.body.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${playlist.songs.size} songs",
            style = typography.caption1,
            color = colors.textSecondary,
            maxLines = 1
        )
    }
}
