package dev.iosfeel.sonora.feature.search

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.navigation.IOSLargeTitleTopBar
import dev.iosfeel.components.search.IOSSearchField
import dev.iosfeel.material.IOSBackdropLayout
import dev.iosfeel.material.rememberIOSBackdropState
import dev.iosfeel.scroll.IOSScrollableLazyColumn
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography
import dev.iosfeel.sonora.core.design.SonoraIcons
import dev.iosfeel.sonora.core.design.artwork.MissingArtwork
import dev.iosfeel.sonora.core.design.artwork.SongArtwork
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.feature.library.songs.SongRow
import dev.iosfeel.sonora.feature.playlist.PlaylistArtwork

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSongOptionsClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val screenBackdrop = rememberIOSBackdropState()

    IOSBackdropLayout(
        state = screenBackdrop,
        modifier = modifier.fillMaxSize(),
        backdrop = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                IOSScrollableLazyColumn(
                    state = listState,
                    topFadeHeight = 24.dp,
                    bottomFadeHeight = 92.dp,
                    contentPadding = PaddingValues(top = 96.dp, bottom = 140.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.statusBarsPadding())
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            IOSSearchField(
                                value = state.query,
                                onValueChange = { viewModel.onQueryChange(it) },
                                placeholder = "Artists, Songs, Lyrics, and More",
                                containerColor = colors.surfaceSecondary,
                                textColor = colors.textPrimary,
                                placeholderColor = colors.textTertiary,
                                cancelTextColor = colors.accent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (state.isQueryEmpty) {
                        // Empty query: Show Recent Searches if any, else Explore Hints
                        if (state.recentSearches.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RECENT SEARCHES",
                                        style = typography.caption1.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = colors.textTertiary
                                        )
                                    )
                                    Text(
                                        text = "Clear",
                                        style = typography.subheadline.copy(color = colors.accent),
                                        modifier = Modifier.clickable {
                                            viewModel.onClearRecentSearches()
                                        }
                                    )
                                }
                            }

                            items(state.recentSearches, key = { it }) { queryItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.onSelectRecentSearch(queryItem) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = SonoraIcons.Search,
                                            contentDescription = null,
                                            tint = colors.textTertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = queryItem,
                                            style = typography.body,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.onRemoveRecentSearch(queryItem) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = SonoraIcons.MoreHorizontal,
                                            contentDescription = "Remove",
                                            tint = colors.textTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp, vertical = 60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = SonoraIcons.Search,
                                            contentDescription = null,
                                            tint = colors.textTertiary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(52.dp)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Search Your Music",
                                            style = typography.title3.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Find songs, albums, artists, and playlists in your local library.",
                                            style = typography.subheadline,
                                            color = colors.textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Non-empty query: Show Search Results or Empty State
                        if (!state.hasResults) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp, vertical = 60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "No Results for \"${state.query}\"",
                                            style = typography.title2.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Check the spelling or try searching for a different song, artist, or album.",
                                            style = typography.subheadline,
                                            color = colors.textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            // 1. Songs Section
                            if (state.songs.isNotEmpty()) {
                                item {
                                    SearchSectionHeader(title = "SONGS")
                                }
                                items(state.songs.take(15), key = { it.id }) { song ->
                                    SongRow(
                                        song = song,
                                        onClick = {
                                            viewModel.onCommitSearch(state.query)
                                            onSongClick(song, state.songs)
                                        },
                                        onOptionsClick = {
                                            onSongOptionsClick(song)
                                        }
                                    )
                                }
                            }

                            // 2. Albums Section
                            if (state.albums.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(18.dp))
                                    SearchSectionHeader(title = "ALBUMS")
                                }
                                items(state.albums, key = { it.id }) { album ->
                                    SearchAlbumRow(
                                        album = album,
                                        onClick = {
                                            viewModel.onCommitSearch(state.query)
                                            onAlbumClick(album)
                                        }
                                    )
                                }
                            }

                            // 3. Artists Section
                            if (state.artists.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(18.dp))
                                    SearchSectionHeader(title = "ARTISTS")
                                }
                                items(state.artists, key = { it.id }) { artist ->
                                    SearchArtistRow(
                                        artist = artist,
                                        onClick = {
                                            viewModel.onCommitSearch(state.query)
                                            onArtistClick(artist)
                                        }
                                    )
                                }
                            }

                            // 4. Playlists Section
                            if (state.playlists.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(18.dp))
                                    SearchSectionHeader(title = "PLAYLISTS")
                                }
                                items(state.playlists, key = { it.id }) { playlist ->
                                    SearchPlaylistRow(
                                        playlist = playlist,
                                        onClick = {
                                            viewModel.onCommitSearch(state.query)
                                            onPlaylistClick(playlist)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        overlay = {
            IOSLargeTitleTopBar(
                title = "Search",
                subtitle = "Sonora",
                scrollState = listState,
                backdrop = screenBackdrop,
                titleColor = colors.textPrimary,
                subtitleColor = colors.accent,
                dividerColor = colors.separator,
                usePillTitle = true,
                titleIcon = {
                    Icon(
                        imageVector = SonoraIcons.Search,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(15.dp)
                    )
                }
            )
        }
    )
}

@Composable
private fun SearchSectionHeader(title: String) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    Text(
        text = title,
        style = typography.caption1.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = colors.textTertiary
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun SearchAlbumRow(
    album: Album,
    onClick: () -> Unit
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (album.songs.isNotEmpty()) {
            SongArtwork(
                song = album.songs.first(),
                cornerRadius = 8.dp,
                modifier = Modifier.size(50.dp)
            )
        } else {
            MissingArtwork(
                modifier = Modifier.size(50.dp),
                cornerRadius = 8.dp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                style = typography.body,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${album.artist} • ${album.songCount} songs",
                style = typography.subheadline,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "›",
            style = typography.title3,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun SearchArtistRow(
    artist: Artist,
    onClick: () -> Unit
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(colors.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SonoraIcons.MusicNote,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = typography.body,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.songCount} songs • ${artist.albumCount} albums",
                style = typography.subheadline,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "›",
            style = typography.title3,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun SearchPlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistArtwork(
            playlist = playlist,
            cornerRadius = 8.dp,
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = typography.body,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.songs.size} songs • ${playlist.formattedDuration}",
                style = typography.subheadline,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "›",
            style = typography.title3,
            color = colors.textTertiary
        )
    }
}
