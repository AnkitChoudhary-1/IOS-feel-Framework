package dev.iosfeel.sonora.core.di

import android.content.Context
import dev.iosfeel.sonora.core.database.SonoraDatabase
import dev.iosfeel.sonora.core.datastore.SonoraPreferences
import dev.iosfeel.sonora.core.media.AndroidMediaStoreMusicLibrary
import dev.iosfeel.sonora.core.media.MusicLibrarySource
import dev.iosfeel.sonora.core.media.PlaybackController
import dev.iosfeel.sonora.core.media.controller.MediaControllerConnection
import dev.iosfeel.sonora.core.media.controller.SonoraPlaybackController
import dev.iosfeel.sonora.core.repository.DefaultMusicLibraryRepository
import dev.iosfeel.sonora.core.repository.MusicLibraryRepository

class SonoraContainer private constructor(
    private val context: Context
) {
    val database: SonoraDatabase by lazy {
        SonoraDatabase.getInstance(context)
    }

    val preferences: SonoraPreferences by lazy {
        SonoraPreferences(context)
    }

    val librarySource: MusicLibrarySource by lazy {
        AndroidMediaStoreMusicLibrary(context)
    }

    val musicRepository: MusicLibraryRepository by lazy {
        DefaultMusicLibraryRepository(
            source = librarySource,
            contentResolver = context.contentResolver
        )
    }

    val controllerConnection: MediaControllerConnection by lazy {
        MediaControllerConnection(context)
    }

    val playbackController: PlaybackController by lazy {
        SonoraPlaybackController(
            connection = controllerConnection,
            historyDao = database.historyDao()
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: SonoraContainer? = null

        fun getInstance(context: Context): SonoraContainer {
            return INSTANCE ?: synchronized(this) {
                val instance = SonoraContainer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
