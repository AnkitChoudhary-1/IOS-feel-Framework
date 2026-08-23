package dev.iosfeel.dayline.core.model

import java.time.Instant
import java.time.LocalDate

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val attachedDate: LocalDate? = null,
    val attachedTaskId: Long? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class FocusSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int,
    val startInstant: Instant,
    val endInstant: Instant? = null,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now()
)
