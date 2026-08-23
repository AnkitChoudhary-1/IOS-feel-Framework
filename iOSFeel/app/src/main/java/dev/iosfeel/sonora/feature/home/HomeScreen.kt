package dev.iosfeel.sonora.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
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
    onArtistClick: (Artist) -> Unit = {},
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
        topFadeHeight = 16.dp,
        bottomFadeHeight = 84.dp,
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

        if (uiState.mostPlayed.isNotEmpty()) {
            item {
                MostPlayedSection(
                    songs = uiState.mostPlayed,
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

        if (uiState.isEmpty) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Music Found Yet",
                            style = typography.title2.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant audio permissions in Library or add audio files to your device storage to start listening.",
                            style = typography.body,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToLibrary,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Go to Library",
                                style = typography.headline.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}
