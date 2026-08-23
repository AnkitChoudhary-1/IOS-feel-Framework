package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.model.MusicLibrary
import kotlinx.coroutines.flow.Flow

interface MusicLibraryRepository {
    suspend fun loadLibrary(): MusicLibrary
    fun observeLibrary(): Flow<MusicLibrary>
}
