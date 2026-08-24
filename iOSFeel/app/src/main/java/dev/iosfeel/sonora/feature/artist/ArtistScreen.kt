package dev.iosfeel.sonora.feature.artist

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
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.Song

@Composable
fun ArtistScreen(
    artist: Artist?,
    currentPlayingSongId: Long?,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    backdrop: IOSBackdropState? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val listState = rememberLazyListState()

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
                    onClick = onBack,
                    backdrop = backdrop
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

    // Normalized progress for compact nav bar title fade-in
    val navTitleAlpha by remember {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            if (firstIndex > 0) 1f else (scrollOffset / 300f).coerceIn(0f, 1f)
        }
    }

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
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(56.dp))
                ArtistHeader(
                    artist = artist,
                    onPlayAll = { onPlayAll(allSongs) },
                    onShuffleAll = { onShuffleAll(allSongs) }
                )
            }

            if (artist.albums.isNotEmpty()) {
                item {
                    ArtistAlbums(
                        albums = artist.albums,
                        onAlbumClick = onAlbumClick
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            if (allSongs.isNotEmpty()) {
                item {
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
            backdrop = backdrop,
            navigation = {
                IOSFloatingIconButton(
                    onClick = onBack,
                    backdrop = backdrop
                ) {
                    Icon(
                        imageVector = SonoraIcons.ChevronLeft,
                        contentDescription = "Back",
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
    }
}
