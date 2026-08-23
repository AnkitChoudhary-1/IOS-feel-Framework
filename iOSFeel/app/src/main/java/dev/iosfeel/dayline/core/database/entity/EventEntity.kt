package dev.iosfeel.dayline.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.iosfeel.dayline.core.model.Event
import java.time.Instant
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
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

fun EventEntity.toDomain(): Event = Event(
    id = id,
    title = title,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    location = location,
    description = description,
    isAllDay = isAllDay,
    reminderEnabled = reminderEnabled,
    createdAt = createdAt
)

fun Event.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    location = location,
    description = description,
    isAllDay = isAllDay,
    reminderEnabled = reminderEnabled,
    createdAt = createdAt
)
