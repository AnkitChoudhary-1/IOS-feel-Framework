package dev.iosfeel.dayline.core.repository

import dev.iosfeel.dayline.core.database.dao.TaskDao
import dev.iosfeel.dayline.core.database.entity.TaskEntity
import dev.iosfeel.dayline.core.database.entity.toDomain
import dev.iosfeel.dayline.core.database.entity.toEntity
import dev.iosfeel.dayline.core.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate

class TaskRepository(
    private val taskDao: TaskDao
) {
    fun getTasksForDate(date: LocalDate): Flow<List<Task>> {
        return taskDao.getTasksForDate(date).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getUnscheduledTasks(): Flow<List<Task>> {
        return taskDao.getUnscheduledTasks().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    suspend fun createTask(task: Task): Long {
        return taskDao.insertTask(task.toEntity())
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    suspend fun toggleTaskCompleted(taskId: Long, isCurrentlyCompleted: Boolean) {
        val nextCompleted = !isCurrentlyCompleted
        val completedAt = if (nextCompleted) Instant.now().toEpochMilli() else null
        taskDao.setTaskCompleted(
            id = taskId,
            completed = nextCompleted,
            completedAt = completedAt
        )
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }
}
