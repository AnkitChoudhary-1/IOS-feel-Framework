package dev.iosfeel.dayline.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.iosfeel.dayline.core.database.dao.EventDao
import dev.iosfeel.dayline.core.database.dao.ExpenseDao
import dev.iosfeel.dayline.core.database.dao.FocusSessionDao
import dev.iosfeel.dayline.core.database.dao.HabitDao
import dev.iosfeel.dayline.core.database.dao.NoteDao
import dev.iosfeel.dayline.core.database.dao.TaskDao
import dev.iosfeel.dayline.core.database.entity.EventEntity
import dev.iosfeel.dayline.core.database.entity.ExpenseEntity
import dev.iosfeel.dayline.core.database.entity.FocusSessionEntity
import dev.iosfeel.dayline.core.database.entity.HabitCompletionEntity
import dev.iosfeel.dayline.core.database.entity.HabitEntity
import dev.iosfeel.dayline.core.database.entity.NoteEntity
import dev.iosfeel.dayline.core.database.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        EventEntity::class,
        ExpenseEntity::class,
        NoteEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DaylineDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun eventDao(): EventDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun noteDao(): NoteDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var instance: DaylineDatabase? = null

        fun getInstance(context: Context): DaylineDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaylineDatabase::class.java,
                    "dayline.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
