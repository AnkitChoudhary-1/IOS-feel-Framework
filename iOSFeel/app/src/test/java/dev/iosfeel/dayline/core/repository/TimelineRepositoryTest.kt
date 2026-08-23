package dev.iosfeel.dayline.core.repository

import dev.iosfeel.dayline.core.database.dao.EventDao
import dev.iosfeel.dayline.core.database.dao.ExpenseDao
import dev.iosfeel.dayline.core.database.dao.HabitDao
import dev.iosfeel.dayline.core.database.dao.TaskDao
import dev.iosfeel.dayline.core.database.entity.EventEntity
import dev.iosfeel.dayline.core.database.entity.ExpenseEntity
import dev.iosfeel.dayline.core.database.entity.HabitCompletionEntity
import dev.iosfeel.dayline.core.database.entity.HabitEntity
import dev.iosfeel.dayline.core.database.entity.TaskEntity
import dev.iosfeel.dayline.core.model.ExpenseCategory
import dev.iosfeel.dayline.core.model.HabitFrequency
import dev.iosfeel.dayline.core.model.TaskPriority
import dev.iosfeel.dayline.core.model.TimelineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimelineRepositoryTest {

    private class FakeEventDao : EventDao {
        val events = mutableListOf<EventEntity>()
        override fun getAllEvents(): Flow<List<EventEntity>> = flowOf(events)
        override fun getEventsForDate(datePrefix: String): Flow<List<EventEntity>> =
            flowOf(events.filter { it.startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE) == datePrefix })
        override suspend fun insertEvent(event: EventEntity): Long { events.add(event); return 1L }
        override suspend fun updateEvent(event: EventEntity) {}
        override suspend fun deleteEvent(event: EventEntity) {}
    }

    private class FakeExpenseDao : ExpenseDao {
        val expenses = mutableListOf<ExpenseEntity>()
        override fun getAllExpenses(): Flow<List<ExpenseEntity>> = flowOf(expenses)
        override fun getExpensesForDate(datePrefix: String): Flow<List<ExpenseEntity>> =
            flowOf(expenses.filter { it.dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE) == datePrefix })
        override suspend fun insertExpense(expense: ExpenseEntity): Long { expenses.add(expense); return 1L }
        override suspend fun deleteExpense(expense: ExpenseEntity) {}
    }

    private class FakeHabitDao : HabitDao {
        val habits = mutableListOf<HabitEntity>()
        val completions = mutableListOf<HabitCompletionEntity>()

        override fun getAllHabits(): Flow<List<HabitEntity>> = flowOf(habits)
        override fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletionEntity>> =
            flowOf(completions.filter { it.completedDate == date })
        override fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>> =
            flowOf(completions.filter { it.habitId == habitId })
        override suspend fun insertHabit(habit: HabitEntity): Long { habits.add(habit); return 1L }
        override suspend fun updateHabit(habit: HabitEntity) {}
        override suspend fun deleteHabit(habit: HabitEntity) {}
        override suspend fun insertCompletion(completion: HabitCompletionEntity): Long { completions.add(completion); return 1L }
        override suspend fun deleteCompletion(habitId: Long, date: LocalDate) {
            completions.removeAll { it.habitId == habitId && it.completedDate == date }
        }
    }

    private class FakeTaskDao : TaskDao {
        val tasks = mutableListOf<TaskEntity>()
        override fun getAllTasks(): Flow<List<TaskEntity>> = flowOf(tasks)
        override fun getTasksForDate(date: LocalDate): Flow<List<TaskEntity>> =
            flowOf(tasks.filter { it.scheduledDate == date })
        override fun getUnscheduledTasks(): Flow<List<TaskEntity>> = flowOf(emptyList())
        override suspend fun getTaskById(id: Long): TaskEntity? = tasks.firstOrNull { it.id == id }
        override suspend fun insertTask(task: TaskEntity): Long { tasks.add(task); return 1L }
        override suspend fun updateTask(task: TaskEntity) {}
        override suspend fun deleteTask(task: TaskEntity) {}
        override suspend fun setTaskCompleted(id: Long, completed: Boolean, completedAt: Long?) {}
    }

    @Test
    fun getTimelineForDate_sortsChronologically() = runBlocking {
        val taskDao = FakeTaskDao()
        val eventDao = FakeEventDao()
        val expenseDao = FakeExpenseDao()
        val habitDao = FakeHabitDao()

        val repo = TimelineRepository(taskDao, eventDao, expenseDao, habitDao)
        val today = LocalDate.of(2026, 8, 23)

        // 1. Task at 14:30
        taskDao.insertTask(
            TaskEntity(
                id = 1,
                title = "Work on iOSFeel",
                scheduledDate = today,
                scheduledTime = LocalTime.of(14, 30),
                priority = TaskPriority.High,
                completed = false
            )
        )

        // 2. Event at 09:30
        eventDao.insertEvent(
            EventEntity(
                id = 2,
                title = "College",
                startDateTime = LocalDateTime.of(2026, 8, 23, 9, 30, 0),
                endDateTime = LocalDateTime.of(2026, 8, 23, 12, 0, 0)
            )
        )

        // 3. Expense at 17:00
        expenseDao.insertExpense(
            ExpenseEntity(
                id = 3,
                amount = 420.0,
                currency = "₹",
                title = "Buy groceries",
                category = ExpenseCategory.Food,
                dateTime = LocalDateTime.of(2026, 8, 23, 17, 0, 0)
            )
        )

        val timeline = repo.getTimelineForDate(today).first()
        assertEquals(3, timeline.size)

        // Pending items sorted ascending by time: 09:30 -> 14:30 -> 17:00 (expense is marked completed)
        assertEquals("College", timeline[0].title)
        assertEquals("Work on iOSFeel", timeline[1].title)
        assertEquals("Buy groceries · ₹420.0", timeline[2].title)
    }

    @Test
    fun calculateNowItem_identifiesNextActiveTask() {
        val taskDao = FakeTaskDao()
        val eventDao = FakeEventDao()
        val expenseDao = FakeExpenseDao()
        val habitDao = FakeHabitDao()

        val repo = TimelineRepository(taskDao, eventDao, expenseDao, habitDao)

        val task1 = TimelineItem.TaskItem(
            dev.iosfeel.dayline.core.model.Task(
                id = 1,
                title = "Morning run",
                scheduledTime = LocalTime.of(8, 0),
                completed = true
            )
        )

        val task2 = TimelineItem.TaskItem(
            dev.iosfeel.dayline.core.model.Task(
                id = 2,
                title = "Work on iOSFeel",
                scheduledTime = LocalTime.of(14, 30),
                completed = false
            )
        )

        val nowState = repo.calculateNowItem(
            items = listOf(task1, task2),
            currentTime = LocalTime.of(10, 0)
        )

        assertEquals("Work on iOSFeel", nowState.title)
        assertEquals(0.5f, nowState.progressFraction, 0.01f)
        assertNotNull(nowState.item)
    }
}
