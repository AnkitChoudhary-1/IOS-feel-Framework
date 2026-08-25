package dev.iosfeel.sonora.feature.favorites

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.feature.library.songs.SongRow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.sonora.core.design.sheet.SonoraActionItem
import dev.iosfeel.sonora.core.design.sheet.SonoraActionSheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import dev.iosfeel.components.floatingbar.IOSFloatingIconButton
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface

@Composable
fun FavoritesScreen(
    favoriteSongs: List<Song>,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onSongOptionsClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val listState = rememberLazyListState()
    val haptics = rememberIOSHaptics()
    var optionsSheetVisible by remember { mutableStateOf(false) }

    val playPressState = rememberIOSPressSurfaceState()
    val shufflePressState = rememberIOSPressSurfaceState()

    val totalDurationMs = favoriteSongs.sumOf { it.durationMs }
    val formattedDuration = run {
        val totalSec = totalDurationMs / 1000
        val min = totalSec / 60
        val hr = min / 60
        val remMin = min % 60
        if (hr > 0) "$hr hr $remMin min" else "$min min"
    }

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
        label = "title_pill_alpha"
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
            contentPadding = PaddingValues(top = 80.dp, bottom = 140.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF2D55).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = SonoraIcons.HeartFilled,
                                contentDescription = null,
                                tint = Color(0xFFFF2D55),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Favorites",
                                style = typography.largeTitle.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${favoriteSongs.size} Songs • $formattedDuration",
                                style = typography.subheadline,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (favoriteSongs.isNotEmpty()) {
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
                                            onPlayAll(favoriteSongs)
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
                                            onShuffleAll(favoriteSongs)
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
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (favoriteSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = SonoraIcons.Heart,
                                contentDescription = null,
                                tint = colors.textTertiary.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Favorites Yet",
                                style = typography.title2.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the heart on any song or in the player menu to add songs to your favorites.",
                                style = typography.subheadline,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(favoriteSongs, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        onClick = {
                            onSongClick(song, favoriteSongs)
                        },
                        onOptionsClick = {
                            onSongOptionsClick(song)
                        }
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
                                    imageVector = SonoraIcons.HeartFilled,
                                    contentDescription = null,
                                    tint = Color(0xFFFF2D55),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Favorites",
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

            if (favoriteSongs.isNotEmpty()) {
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
        }

        // iOSFeel Action Sheet for Favorites Options
        SonoraActionSheet(
            visible = optionsSheetVisible,
            onDismiss = { optionsSheetVisible = false },
            title = "Favorites",
            subtitle = "${favoriteSongs.size} songs",
            actions = listOf(
                SonoraActionItem(
                    title = "Play All",
                    icon = SonoraIcons.Play,
                    onClick = { onPlayAll(favoriteSongs) }
                ),
                SonoraActionItem(
                    title = "Shuffle All",
                    icon = SonoraIcons.Shuffle,
                    onClick = { onShuffleAll(favoriteSongs) }
                )
            )
        )
    }
}
