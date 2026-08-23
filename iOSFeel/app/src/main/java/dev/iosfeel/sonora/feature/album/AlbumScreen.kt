package dev.iosfeel.sonora.feature.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
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
import dev.iosfeel.sonora.core.model.Song

@Composable
fun AlbumScreen(
    album: Album?,
    currentPlayingSongId: Long?,
    onBack: () -> Unit,
    onArtistClick: (Long) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    backdrop: IOSBackdropState? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val listState = rememberLazyListState()

    if (album == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Album Not Found",
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

    // Normalized progress for compact nav bar title fade-in
    val navTitleAlpha by remember {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            if (firstIndex > 0) 1f else (scrollOffset / 400f).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        IOSScrollableLazyColumn(
            state = listState,
            topFadeHeight = 20.dp,
            bottomFadeHeight = 68.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height(56.dp))
                AlbumHero(
                    album = album,
                    onArtistClick = {
                        album.artistId?.let { onArtistClick(it) }
                    },
                    onPlayAll = { onPlayAll(album.songs) },
                    onShuffleAll = { onShuffleAll(album.songs) }
                )
            }

            itemsIndexed(album.songs, key = { _, song -> song.id }) { index, song ->
                AlbumTrackRow(
                    song = song,
                    trackIndex = index,
                    isPlaying = song.id == currentPlayingSongId,
                    onClick = { onSongClick(song, album.songs) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(160.dp))
            }
        }

        // Modern iOS-Inspired Floating Top Bar
        IOSFloatingTopBar(
            title = album.title,
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
