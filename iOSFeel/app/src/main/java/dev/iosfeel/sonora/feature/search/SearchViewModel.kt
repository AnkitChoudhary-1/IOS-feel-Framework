package dev.iosfeel.sonora.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.iosfeel.sonora.core.datastore.SonoraPreferences
import dev.iosfeel.sonora.core.repository.MusicLibraryRepository
import dev.iosfeel.sonora.core.repository.PlaylistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val musicRepository: MusicLibraryRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferences: SonoraPreferences
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val state: StateFlow<SearchUiState> = combine(
        _query,
        _query.debounce(150).distinctUntilChanged().flatMapLatest { q ->
            val trimmed = q.trim()
            if (trimmed.isBlank()) {
                flowOf(SearchResult())
            } else {
                combine(musicRepository.observeLibrary(), playlistRepository.playlists) { library: dev.iosfeel.sonora.core.model.MusicLibrary, playlists: List<dev.iosfeel.sonora.core.model.Playlist> ->
                    SearchEngine.search(
                        query = trimmed,
                        library = library,
                        playlists = playlists
                    )
                }
            }
        },
        preferences.recentSearches
    ) { currentQuery, searchResult, recentSearches ->
        SearchUiState(
            query = currentQuery,
            searching = false,
            songs = searchResult.songs,
            albums = searchResult.albums,
            artists = searchResult.artists,
            playlists = searchResult.playlists,
            recentSearches = recentSearches
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onCommitSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                preferences.addRecentSearch(trimmed)
            }
        }
    }

    fun onSelectRecentSearch(query: String) {
        _query.value = query
        onCommitSearch(query)
    }

    fun onRemoveRecentSearch(query: String) {
        viewModelScope.launch {
            preferences.removeRecentSearch(query)
        }
    }

    fun onClearRecentSearches() {
        viewModelScope.launch {
            preferences.clearRecentSearches()
        }
    }

    fun onClearQuery() {
        _query.value = ""
    }
}
