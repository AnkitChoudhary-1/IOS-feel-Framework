package dev.iosfeel.dayline.core.di

import android.content.Context
import dev.iosfeel.dayline.core.database.DaylineDatabase
import dev.iosfeel.dayline.core.datastore.DaylinePreferences
import dev.iosfeel.dayline.core.repository.TaskRepository
import dev.iosfeel.dayline.core.repository.TimelineRepository

class DaylineContainer(context: Context) {
    val database: DaylineDatabase by lazy {
        DaylineDatabase.getInstance(context)
    }

    val preferences: DaylinePreferences by lazy {
        DaylinePreferences(context)
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    val timelineRepository: TimelineRepository by lazy {
        TimelineRepository(
            taskDao = database.taskDao(),
            eventDao = database.eventDao(),
            expenseDao = database.expenseDao(),
            habitDao = database.habitDao()
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: DaylineContainer? = null

        fun getInstance(context: Context): DaylineContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DaylineContainer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
