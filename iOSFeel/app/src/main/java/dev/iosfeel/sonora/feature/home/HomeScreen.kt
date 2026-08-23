package dev.iosfeel.sonora.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.feature.home.sections.MostPlayedSection
import dev.iosfeel.sonora.feature.home.sections.RecentlyAddedSection
import dev.iosfeel.sonora.feature.home.sections.RecentlyPlayedSection

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
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

        if (uiState.mostPlayed.isNotEmpty()) {
            item {
                MostPlayedSection(
                    songs = uiState.mostPlayed,
                    onSongClick = onSongClick
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        item {
            uiState.libraryStats?.let { stats ->
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
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
