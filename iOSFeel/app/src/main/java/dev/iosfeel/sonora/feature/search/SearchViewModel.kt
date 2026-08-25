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

data class OnlineSearchData(
    val songs: List<dev.iosfeel.sonora.core.model.Song> = emptyList(),
    val albums: List<dev.iosfeel.sonora.core.model.Album> = emptyList(),
    val artists: List<dev.iosfeel.sonora.core.model.Artist> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val musicRepository: MusicLibraryRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferences: SonoraPreferences,
    private val ytMusicClient: dev.iosfeel.sonora.core.network.ytmusic.YouTubeMusicClient = dev.iosfeel.sonora.core.network.ytmusic.YouTubeMusicClient()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _scope = MutableStateFlow(SearchSourceScope.ALL)
    private val _onlineData = MutableStateFlow(OnlineSearchData())

    init {
        viewModelScope.launch {
            _query.debounce(300).distinctUntilChanged().collect { q ->
                val trimmed = q.trim()
                if (trimmed.length >= 2) {
                    _onlineData.value = _onlineData.value.copy(isLoading = true)
                    try {
                        val result = ytMusicClient.search(trimmed)
                        val songs = result.songs.map { it.toDomainSong() }
                        val albums = result.albums.map { it.toDomainAlbum() }
                        val artists = result.artists.map { it.toDomainArtist() }
                        _onlineData.value = OnlineSearchData(
                            songs = songs,
                            albums = albums,
                            artists = artists,
                            isLoading = false
                        )
                    } catch (e: Exception) {
                        _onlineData.value = OnlineSearchData(isLoading = false)
                    }
                } else {
                    _onlineData.value = OnlineSearchData(isLoading = false)
                }
            }
        }
    }

    private val localSearchResult = _query.debounce(150).distinctUntilChanged().flatMapLatest { q ->
        val trimmed = q.trim()
        if (trimmed.isBlank()) {
            flowOf(SearchResult())
        } else {
            combine(musicRepository.observeLibrary(), playlistRepository.playlists) { library, playlists ->
                SearchEngine.search(
                    query = trimmed,
                    library = library,
                    playlists = playlists
                )
            }
        }
    }

    val state: StateFlow<SearchUiState> = combine(
        combine(_query, _scope, _onlineData) { q, scope, online -> Triple(q, scope, online) },
        localSearchResult,
        preferences.recentSearches
    ) { (query, searchScope, online), local, recentSearches ->
        SearchUiState(
            query = query,
            searching = false,
            isOnlineLoading = online.isLoading,
            searchScope = searchScope,
            songs = if (searchScope == SearchSourceScope.YOUTUBE_MUSIC) emptyList() else local.songs,
            albums = if (searchScope == SearchSourceScope.YOUTUBE_MUSIC) emptyList() else local.albums,
            artists = if (searchScope == SearchSourceScope.YOUTUBE_MUSIC) emptyList() else local.artists,
            playlists = if (searchScope == SearchSourceScope.YOUTUBE_MUSIC) emptyList() else local.playlists,
            onlineSongs = if (searchScope == SearchSourceScope.LOCAL) emptyList() else online.songs,
            onlineAlbums = if (searchScope == SearchSourceScope.LOCAL) emptyList() else online.albums,
            onlineArtists = if (searchScope == SearchSourceScope.LOCAL) emptyList() else online.artists,
            recentSearches = recentSearches
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onScopeChange(newScope: SearchSourceScope) {
        _scope.value = newScope
    }

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
