package dev.iosfeel.dayline.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.iosfeel.dayline.core.model.FocusSession
import dev.iosfeel.dayline.core.model.Note
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val attachedDate: LocalDate? = null,
    val attachedTaskId: Long? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int,
    val startInstant: Instant,
    val endInstant: Instant? = null,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now()
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    attachedDate = attachedDate,
    attachedTaskId = attachedTaskId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    attachedDate = attachedDate,
    attachedTaskId = attachedTaskId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FocusSessionEntity.toDomain(): FocusSession = FocusSession(
    id = id,
    taskId = taskId,
    taskTitle = taskTitle,
    durationMinutes = durationMinutes,
    startInstant = startInstant,
    endInstant = endInstant,
    completed = completed,
    createdAt = createdAt
)

fun FocusSession.toEntity(): FocusSessionEntity = FocusSessionEntity(
    id = id,
    taskId = taskId,
    taskTitle = taskTitle,
    durationMinutes = durationMinutes,
    startInstant = startInstant,
    endInstant = endInstant,
    completed = completed,
    createdAt = createdAt
)
