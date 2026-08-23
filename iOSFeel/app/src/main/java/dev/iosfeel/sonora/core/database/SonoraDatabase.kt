package dev.iosfeel.sonora.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.iosfeel.sonora.core.database.dao.FavoriteDao
import dev.iosfeel.sonora.core.database.dao.HistoryDao
import dev.iosfeel.sonora.core.database.dao.PlaylistDao
import dev.iosfeel.sonora.core.database.entity.FavoriteEntity
import dev.iosfeel.sonora.core.database.entity.PlaybackHistoryEntity
import dev.iosfeel.sonora.core.database.entity.PlaylistEntity
import dev.iosfeel.sonora.core.database.entity.PlaylistSongCrossRef

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlaybackHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SonoraDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: SonoraDatabase? = null

        fun getInstance(context: Context): SonoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonoraDatabase::class.java,
                    "sonora.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
