package dev.iosfeel.dayline.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class DaylineModelTest {

    @Test
    fun taskItem_mapsPropertiesCorrectly() {
        val task = Task(
            id = 101,
            title = "Finish Phase 0",
            scheduledDate = LocalDate.of(2026, 8, 23),
            scheduledTime = LocalTime.of(10, 30),
            priority = TaskPriority.High,
            completed = false
        )

        val item = TimelineItem.TaskItem(task)
        assertEquals("task_101", item.id)
        assertEquals("Finish Phase 0", item.title)
        assertEquals(LocalTime.of(10, 30), item.time)
        assertFalse(item.isCompleted)
    }

    @Test
    fun habitItem_reflectsCompletionState() {
        val habit = Habit(
            id = 202,
            title = "Morning Meditation",
            frequency = HabitFrequency.Daily,
            targetDaysPerWeek = 7
        )

        val uncompletedItem = TimelineItem.HabitItem(habit, completion = null)
        assertEquals("habit_202", uncompletedItem.id)
        assertEquals("Morning Meditation", uncompletedItem.title)
        assertFalse(uncompletedItem.isCompleted)

        val completion = HabitCompletion(
            id = 1,
            habitId = 202,
            completedDate = LocalDate.now()
        )
        val completedItem = TimelineItem.HabitItem(habit, completion = completion)
        assertTrue(completedItem.isCompleted)
    }

    @Test
    fun expenseItem_formatsTitleWithCurrency() {
        val expense = Expense(
            id = 303,
            amount = 420.0,
            currency = "₹",
            title = "Groceries",
            category = ExpenseCategory.Food,
            dateTime = LocalDate.of(2026, 8, 23).atTime(17, 0)
        )

        val item = TimelineItem.ExpenseItem(expense)
        assertEquals("expense_303", item.id)
        assertEquals("Groceries · ₹420.0", item.title)
        assertEquals(LocalTime.of(17, 0), item.time)
        assertTrue(item.isCompleted)
    }
}
