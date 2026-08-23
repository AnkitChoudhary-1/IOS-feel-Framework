package dev.iosfeel.dayline.core.repository

import dev.iosfeel.dayline.core.database.dao.TaskDao
import dev.iosfeel.dayline.core.database.entity.TaskEntity
import dev.iosfeel.dayline.core.model.Task
import dev.iosfeel.dayline.core.model.TaskPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class TaskRepositoryTest {

    private class FakeTaskDao : TaskDao {
        val tasks = mutableListOf<TaskEntity>()

        override fun getAllTasks(): Flow<List<TaskEntity>> = flowOf(tasks)

        override fun getTasksForDate(date: LocalDate): Flow<List<TaskEntity>> =
            flowOf(tasks.filter { it.scheduledDate == date })

        override fun getUnscheduledTasks(): Flow<List<TaskEntity>> =
            flowOf(tasks.filter { it.scheduledDate == null && !it.completed })

        override suspend fun getTaskById(id: Long): TaskEntity? =
            tasks.firstOrNull { it.id == id }

        override suspend fun insertTask(task: TaskEntity): Long {
            val id = if (task.id == 0L) (tasks.size + 1).toLong() else task.id
            val toInsert = task.copy(id = id)
            tasks.add(toInsert)
            return id
        }

        override suspend fun updateTask(task: TaskEntity) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index >= 0) tasks[index] = task
        }

        override suspend fun deleteTask(task: TaskEntity) {
            tasks.removeAll { it.id == task.id }
        }

        override suspend fun setTaskCompleted(id: Long, completed: Boolean, completedAt: Long?) {
            val index = tasks.indexOfFirst { it.id == id }
            if (index >= 0) {
                tasks[index] = tasks[index].copy(
                    completed = completed,
                    completedAt = completedAt?.let { Instant.ofEpochMilli(it) }
                )
            }
        }
    }

    @Test
    fun getTasksForDate_filtersAndMapsCorrectly() = runBlocking {
        val dao = FakeTaskDao()
        val repo = TaskRepository(dao)

        val today = LocalDate.of(2026, 8, 23)
        val tomorrow = today.plusDays(1)

        repo.createTask(
            Task(
                title = "Task 1",
                scheduledDate = today,
                scheduledTime = LocalTime.of(10, 0),
                priority = TaskPriority.High,
                completed = false
            )
        )

        repo.createTask(
            Task(
                title = "Task 2",
                scheduledDate = tomorrow,
                priority = TaskPriority.Low,
                completed = false
            )
        )

        val todayTasks = repo.getTasksForDate(today).first()
        assertEquals(1, todayTasks.size)
        assertEquals("Task 1", todayTasks[0].title)
        assertEquals(TaskPriority.High, todayTasks[0].priority)
    }

    @Test
    fun toggleTaskCompleted_updatesState() = runBlocking {
        val dao = FakeTaskDao()
        val repo = TaskRepository(dao)

        val id = repo.createTask(
            Task(
                title = "Task to toggle",
                completed = false
            )
        )

        repo.toggleTaskCompleted(id, isCurrentlyCompleted = false)
        val updated = repo.getTaskById(id)
        assertTrue(updated?.completed == true)

        repo.toggleTaskCompleted(id, isCurrentlyCompleted = true)
        val uncompleted = repo.getTaskById(id)
        assertFalse(uncompleted?.completed == true)
    }
}
