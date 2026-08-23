package dev.iosfeel.sonora.core.di

import android.content.Context
import dev.iosfeel.sonora.core.database.SonoraDatabase
import dev.iosfeel.sonora.core.datastore.SonoraPreferences
import dev.iosfeel.sonora.core.media.MediaStoreScanner

class SonoraContainer private constructor(
    private val context: Context
) {
    val database: SonoraDatabase by lazy {
        SonoraDatabase.getInstance(context)
    }

    val preferences: SonoraPreferences by lazy {
        SonoraPreferences(context)
    }

    val scanner: MediaStoreScanner by lazy {
        MediaStoreScanner(context)
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
