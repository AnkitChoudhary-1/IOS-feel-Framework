package dev.iosfeel.dayline.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.iosfeel.dayline.core.model.Task
import dev.iosfeel.dayline.core.model.TaskPriority
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val scheduledDate: LocalDate? = null,
    val scheduledTime: LocalTime? = null,
    val dueDate: LocalDate? = null,
    val estimatedMinutes: Int? = null,
    val priority: TaskPriority = TaskPriority.None,
    val completed: Boolean = false,
    val completedAt: Instant? = null,
    val reminderEnabled: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    scheduledDate = scheduledDate,
    scheduledTime = scheduledTime,
    dueDate = dueDate,
    estimatedMinutes = estimatedMinutes,
    priority = priority,
    completed = completed,
    completedAt = completedAt,
    reminderEnabled = reminderEnabled,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    scheduledDate = scheduledDate,
    scheduledTime = scheduledTime,
    dueDate = dueDate,
    estimatedMinutes = estimatedMinutes,
    priority = priority,
    completed = completed,
    completedAt = completedAt,
    reminderEnabled = reminderEnabled,
    createdAt = createdAt,
    updatedAt = updatedAt
)
