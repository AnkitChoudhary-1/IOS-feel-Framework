package dev.iosfeel.dayline.core.model

import java.time.Instant
import java.time.LocalDateTime

data class Event(
    val id: Long = 0,
    val title: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val location: String? = null,
    val description: String? = null,
    val isAllDay: Boolean = false,
    val reminderEnabled: Boolean = false,
    val createdAt: Instant = Instant.now()
)
