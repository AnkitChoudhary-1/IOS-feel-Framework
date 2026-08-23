package dev.iosfeel.dayline.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

enum class TaskPriority {
    Low,
    Medium,
    High
}

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val scheduledDate: LocalDate? = null,
    val scheduledTime: LocalTime? = null,
    val dueDate: LocalDate? = null,
    val estimatedMinutes: Int? = null,
    val priority: TaskPriority = TaskPriority.Medium,
    val completed: Boolean = false,
    val completedAt: Instant? = null,
    val reminderEnabled: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
