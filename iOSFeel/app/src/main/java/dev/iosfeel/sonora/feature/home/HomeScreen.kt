package dev.iosfeel.sonora.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.button.IOSButton
import dev.iosfeel.components.button.IOSButtonStyle
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.feature.home.sections.HomeArtistsSection
import dev.iosfeel.sonora.feature.home.sections.MostPlayedSection
import dev.iosfeel.sonora.feature.home.sections.QuickPicksSection
import dev.iosfeel.sonora.feature.home.sections.RecentlyAddedSection
import dev.iosfeel.sonora.feature.home.sections.RecentlyPlayedSection

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val listState = rememberLazyListState()

    if (uiState.loading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = colors.accent,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        }
        return
    }

    IOSScrollableLazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Sonora",
                    style = typography.subheadline.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent
                )
                Text(
                    text = "Listen Now",
                    style = typography.largeTitle.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            }
        }

        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                RecentlyPlayedSection(
                    songs = uiState.recentlyPlayed,
                    onSongClick = onSongClick
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (uiState.recentlyAdded.isNotEmpty()) {
            item {
                RecentlyAddedSection(
                    albums = uiState.recentlyAdded,
                    onAlbumClick = onAlbumClick
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (uiState.quickPicks.isNotEmpty()) {
            item {
                QuickPicksSection(
                    songs = uiState.quickPicks,
                    onSongClick = onSongClick
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (uiState.recentArtists.isNotEmpty()) {
            item {
                HomeArtistsSection(
                    artists = uiState.recentArtists,
                    onArtistClick = onArtistClick
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (uiState.mostPlayed.isNotEmpty()) {
            item {
                MostPlayedSection(
                    songs = uiState.mostPlayed,
                    onSongClick = onSongClick
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (uiState.isEmpty) {
            item {
                EmptyHomeView(onScanClick = onNavigateToLibrary)
            }
        }

        item {
            uiState.libraryStats?.let { stats ->
                if (stats.songs > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = "${stats.songs} songs · ${stats.albums} albums · ${stats.artists} artists",
                            style = typography.caption1,
                            color = colors.textTertiary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun EmptyHomeView(onScanClick: () -> Unit) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = SonoraIcons.MusicNote,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Discover Your Music",
                style = typography.title2,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Grant permission or scan your audio library to see your songs and albums here.",
                style = typography.subheadline,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            IOSButton(
                text = "Go to Library",
                onClick = onScanClick,
                style = IOSButtonStyle.Filled
            )
        }
    }
}
