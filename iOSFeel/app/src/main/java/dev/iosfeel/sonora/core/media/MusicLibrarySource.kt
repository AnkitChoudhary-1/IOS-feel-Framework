package dev.iosfeel.sonora.core.media

import dev.iosfeel.sonora.core.model.Song

interface MusicLibrarySource {
    suspend fun getSongs(): List<Song>
}
