package dev.iosfeel.sonora.feature.artist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.floatingbar.IOSFloatingIconButton
import dev.iosfeel.components.floatingbar.IOSFloatingTopBar
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.Song

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.iosfeel.sonora.core.design.sheet.SonoraActionItem
import dev.iosfeel.sonora.core.design.sheet.SonoraActionSheet

@Composable
fun ArtistScreen(
    artist: Artist?,
    currentPlayingSongId: Long?,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val listState = rememberLazyListState()
    var optionsSheetVisible by remember { mutableStateOf(false) }

    if (artist == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Artist Not Found",
                    style = typography.title2,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                IOSFloatingIconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = SonoraIcons.ChevronLeft,
                        contentDescription = "Back",
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        return
    }

    val allSongs = remember(artist) {
        artist.albums.flatMap { it.songs }
    }

    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset > 40 || listState.firstVisibleItemIndex > 0 }
    }

    val navTitleAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
        label = "nav_title_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        IOSScrollableLazyColumn(
            state = listState,
            topFadeHeight = 24.dp,
            bottomFadeHeight = 72.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ArtistHeader(
                    artist = artist,
                    onPlayAll = { onPlayAll(allSongs) },
                    onShuffleAll = { onShuffleAll(allSongs) }
                )
            }

            if (artist.albums.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ArtistAlbums(
                        albums = artist.albums,
                        onAlbumClick = onAlbumClick
                    )
                }
            }

            if (allSongs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    ArtistSongs(
                        songs = allSongs,
                        currentPlayingSongId = currentPlayingSongId,
                        onSongClick = onSongClick
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(160.dp))
            }
        }

        // Modern iOS-Inspired Floating Top Bar
        IOSFloatingTopBar(
            title = artist.name,
            titleAlpha = navTitleAlpha,
            titleColor = colors.textPrimary,
            navigation = {
                IOSFloatingIconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = SonoraIcons.ChevronLeft,
                        contentDescription = "Back",
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            actions = {
                if (allSongs.isNotEmpty()) {
                    IOSFloatingIconButton(
                        onClick = { optionsSheetVisible = true }
                    ) {
                        Icon(
                            imageVector = SonoraIcons.MoreHorizontal,
                            contentDescription = "Options",
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        )

        // Artist Action Sheet
        SonoraActionSheet(
            visible = optionsSheetVisible,
            onDismiss = { optionsSheetVisible = false },
            title = artist.name,
            subtitle = "${allSongs.size} songs",
            actions = listOf(
                SonoraActionItem(
                    title = "Play All",
                    icon = SonoraIcons.Play,
                    onClick = { onPlayAll(allSongs) }
                ),
                SonoraActionItem(
                    title = "Shuffle All",
                    icon = SonoraIcons.Shuffle,
                    onClick = { onShuffleAll(allSongs) }
                )
            )
        )
    }
}
