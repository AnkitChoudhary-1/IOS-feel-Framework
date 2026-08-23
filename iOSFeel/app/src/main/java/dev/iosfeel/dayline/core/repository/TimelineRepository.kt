package dev.iosfeel.dayline.core.repository

import dev.iosfeel.dayline.core.database.dao.EventDao
import dev.iosfeel.dayline.core.database.dao.ExpenseDao
import dev.iosfeel.dayline.core.database.dao.HabitDao
import dev.iosfeel.dayline.core.database.dao.TaskDao
import dev.iosfeel.dayline.core.database.entity.toDomain
import dev.iosfeel.dayline.core.model.TimelineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class NowItemState(
    val title: String,
    val subtitle: String,
    val progressFraction: Float,
    val item: TimelineItem? = null
)

class TimelineRepository(
    private val taskDao: TaskDao,
    private val eventDao: EventDao,
    private val expenseDao: ExpenseDao,
    private val habitDao: HabitDao
) {
    fun getTimelineForDate(date: LocalDate): Flow<List<TimelineItem>> {
        val datePrefix = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        return combine(
            taskDao.getTasksForDate(date),
            eventDao.getEventsForDate(datePrefix),
            expenseDao.getExpensesForDate(datePrefix),
            habitDao.getAllHabits()
        ) { taskEntities, eventEntities, expenseEntities, habitEntities ->
            val items = mutableListOf<TimelineItem>()

            // 1. Map tasks
            taskEntities.forEach { entity ->
                items.add(TimelineItem.TaskItem(entity.toDomain()))
            }

            // 2. Map events
            eventEntities.forEach { entity ->
                items.add(TimelineItem.EventItem(entity.toDomain()))
            }

            // 3. Map expenses
            expenseEntities.forEach { entity ->
                items.add(TimelineItem.ExpenseItem(entity.toDomain()))
            }

            // 4. Map active habits
            habitEntities.forEach { entity ->
                items.add(TimelineItem.HabitItem(entity.toDomain(), completion = null))
            }

            // Sort chronologically:
            // Pending items with a specific time are sorted ascending by time.
            // Items without time are placed at the beginning/end cleanly.
            items.sortedWith(
                compareBy<TimelineItem> { it.isCompleted }
                    .thenBy { it.time ?: LocalTime.MAX }
            )
        }
    }

    fun calculateNowItem(items: List<TimelineItem>, currentTime: LocalTime = LocalTime.now()): NowItemState {
        val pendingItems = items.filter { !it.isCompleted }

        if (pendingItems.isEmpty()) {
            val totalCount = items.size
            return if (totalCount > 0) {
                NowItemState(
                    title = "All done for today",
                    subtitle = "$totalCount items completed",
                    progressFraction = 1.0f,
                    item = null
                )
            } else {
                NowItemState(
                    title = "Plan your day",
                    subtitle = "Set 3 priorities to get started",
                    progressFraction = 0.0f,
                    item = null
                )
            }
        }

        // Find current or next closest pending item
        val activeItem = pendingItems.firstOrNull { it.time != null && it.time!! >= currentTime }
            ?: pendingItems.first()

        val subtitle = when {
            activeItem.time != null -> "Scheduled at ${activeItem.time}"
            else -> "Next priority"
        }

        val completedCount = items.count { it.isCompleted }
        val fraction = if (items.isNotEmpty()) completedCount.toFloat() / items.size.toFloat() else 0f

        return NowItemState(
            title = activeItem.title,
            subtitle = subtitle,
            progressFraction = fraction,
            item = activeItem
        )
    }
}
