package dev.iosfeel.sonora.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.dateAddedSeconds
import dev.iosfeel.sonora.core.model.resolveSongs
import dev.iosfeel.sonora.core.model.stats
import dev.iosfeel.sonora.core.repository.FavoritesRepository
import dev.iosfeel.sonora.core.repository.MusicLibraryRepository
import dev.iosfeel.sonora.core.repository.PlaybackHistoryRepository
import dev.iosfeel.sonora.core.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class OnlineExploreData(
    val trending: List<dev.iosfeel.sonora.core.model.Song> = emptyList(),
    val newReleases: List<dev.iosfeel.sonora.core.model.Album> = emptyList()
)

class HomeViewModel(
    private val musicRepository: MusicLibraryRepository,
    private val historyRepository: PlaybackHistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val playlistRepository: PlaylistRepository,
    private val ytMusicClient: dev.iosfeel.sonora.core.network.ytmusic.YouTubeMusicClient = dev.iosfeel.sonora.core.network.ytmusic.YouTubeMusicClient()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(loading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _onlineExplore = MutableStateFlow(OnlineExploreData())

    init {
        viewModelScope.launch {
            try {
                val explore = ytMusicClient.getExplore()
                val trending = explore.trendingSongs.map { it.toDomainSong() }
                val newReleases = explore.newReleases.map { it.toDomainAlbum() }
                _onlineExplore.value = OnlineExploreData(trending, newReleases)
            } catch (e: Exception) {
                // Keep empty on network error
            }
        }

        viewModelScope.launch {
            val localFlow = combine(
                musicRepository.observeLibrary(),
                historyRepository.observeRecentlyPlayedIds(20),
                historyRepository.observeMostPlayedIds(20),
                favoritesRepository.observeFavoriteSongs(musicRepository),
                playlistRepository.playlists
            ) { library: MusicLibrary, recentIds: List<Long>, mostPlayedIds: List<Long>, favorites, playlists ->
                val recentlyPlayedSongs = library.resolveSongs(recentIds)
                val mostPlayedSongs = library.resolveSongs(mostPlayedIds)
                val recentlyAddedAlbums = library.albums
                    .sortedByDescending { it.dateAddedSeconds }
                    .take(12)

                val quickPicks = library.songs.take(10)
                val featuredAlbums = library.albums.take(10)

                HomeUiState(
                    loading = false,
                    recentlyPlayed = recentlyPlayedSongs,
                    recentlyAdded = recentlyAddedAlbums,
                    favorites = favorites.take(10),
                    playlists = playlists.take(10),
                    mostPlayed = mostPlayedSongs,
                    quickPicks = quickPicks,
                    featuredAlbums = featuredAlbums,
                    recentArtists = library.artists.take(10),
                    libraryStats = library.stats()
                )
            }

            combine(localFlow, _onlineExplore) { localState, online ->
                localState.copy(
                    trendingOnline = online.trending,
                    newReleasesOnline = online.newReleases
                )
            }.collect { uiState ->
                _state.value = uiState
            }
        }
    }
}
