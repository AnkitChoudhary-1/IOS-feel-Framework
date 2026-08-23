package dev.iosfeel.dayline.core.model

import java.time.LocalTime

sealed interface TimelineItem {
    val id: String
    val title: String
    val time: LocalTime?
    val isCompleted: Boolean

    data class TaskItem(
        val task: Task
    ) : TimelineItem {
        override val id: String get() = "task_${task.id}"
        override val title: String get() = task.title
        override val time: LocalTime? get() = task.scheduledTime
        override val isCompleted: Boolean get() = task.completed
    }

    data class HabitItem(
        val habit: Habit,
        val completion: HabitCompletion? = null
    ) : TimelineItem {
        override val id: String get() = "habit_${habit.id}"
        override val title: String get() = habit.title
        override val time: LocalTime? get() = null
        override val isCompleted: Boolean get() = completion != null
    }

    data class EventItem(
        val event: Event
    ) : TimelineItem {
        override val id: String get() = "event_${event.id}"
        override val title: String get() = event.title
        override val time: LocalTime get() = event.startDateTime.toLocalTime()
        override val isCompleted: Boolean get() = false
    }

    data class ExpenseItem(
        val expense: Expense
    ) : TimelineItem {
        override val id: String get() = "expense_${expense.id}"
        override val title: String get() = "${expense.title} · ${expense.currency}${expense.amount}"
        override val time: LocalTime get() = expense.dateTime.toLocalTime()
        override val isCompleted: Boolean get() = true
    }

    data class FocusItem(
        val focusSession: FocusSession
    ) : TimelineItem {
        override val id: String get() = "focus_${focusSession.id}"
        override val title: String get() = focusSession.taskTitle ?: "Focus Session"
        override val time: LocalTime? get() = null
        override val isCompleted: Boolean get() = focusSession.completed
    }
}
