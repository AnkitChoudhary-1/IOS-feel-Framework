package dev.iosfeel.sonora.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.LibrarySection
import dev.iosfeel.sonora.core.model.SongSort
import dev.iosfeel.sonora.core.model.SortDirection
import dev.iosfeel.sonora.core.repository.MusicLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: MusicLibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState())
    val state = _state.asStateFlow()

    fun permissionChanged(granted: Boolean) {
        _state.update { it.copy(permissionGranted = granted) }
        if (granted) {
            observeLibraryChanges()
            refresh()
        }
    }

    fun selectSection(section: LibrarySection) {
        _state.update { it.copy(section = section, selectedAlbum = null, selectedArtist = null) }
    }

    fun selectSort(sort: SongSort) {
        _state.update {
            if (it.songSort == sort) {
                val newDir = if (it.sortDirection == SortDirection.Ascending) SortDirection.Descending else SortDirection.Ascending
                it.copy(sortDirection = newDir)
            } else {
                it.copy(songSort = sort, sortDirection = SortDirection.Ascending)
            }
        }
    }

    fun openAlbum(album: Album) {
        _state.update { it.copy(selectedAlbum = album) }
    }

    fun closeAlbum() {
        _state.update { it.copy(selectedAlbum = null) }
    }

    fun openArtist(artist: Artist) {
        _state.update { it.copy(selectedArtist = artist) }
    }

    fun closeArtist() {
        _state.update { it.copy(selectedArtist = null) }
    }

    fun refresh() {
        if (!_state.value.permissionGranted) return

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            runCatching {
                repository.loadLibrary()
            }.onSuccess { library ->
                _state.update { it.copy(loading = false, library = library) }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        loading = false,
                        error = LibraryError.Unknown(throwable)
                    )
                }
            }
        }
    }

    private fun observeLibraryChanges() {
        viewModelScope.launch {
            repository.observeLibrary().collect { library ->
                _state.update { it.copy(library = library) }
            }
        }
    }
}
