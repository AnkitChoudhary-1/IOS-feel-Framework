package dev.iosfeel.sonora.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.dateAddedSeconds
import dev.iosfeel.sonora.core.model.resolveSongs
import dev.iosfeel.sonora.core.model.stats
import dev.iosfeel.sonora.core.repository.MusicLibraryRepository
import dev.iosfeel.sonora.core.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val musicRepository: MusicLibraryRepository,
    private val historyRepository: PlaybackHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(loading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                musicRepository.observeLibrary(),
                historyRepository.observeRecentlyPlayedIds(20),
                historyRepository.observeMostPlayedIds(20)
            ) { library: MusicLibrary, recentIds: List<Long>, mostPlayedIds: List<Long> ->
                val recentlyPlayedSongs = library.resolveSongs(recentIds)
                val mostPlayedSongs = library.resolveSongs(mostPlayedIds)
                val recentlyAddedAlbums = library.albums
                    .sortedByDescending { it.dateAddedSeconds }
                    .take(12)

                // Quick picks from library (first 10 songs or shuffled representation)
                val quickPicks = library.songs.take(10)
                val featuredAlbums = library.albums.take(10)

                HomeUiState(
                    loading = false,
                    recentlyPlayed = recentlyPlayedSongs,
                    recentlyAdded = recentlyAddedAlbums,
                    mostPlayed = mostPlayedSongs,
                    quickPicks = quickPicks,
                    featuredAlbums = featuredAlbums,
                    recentArtists = library.artists.take(10),
                    libraryStats = library.stats()
                )
            }.collect { uiState ->
                _state.value = uiState
            }
        }
    }
}
