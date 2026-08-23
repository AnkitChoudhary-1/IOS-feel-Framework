package dev.iosfeel.dayline.core.model

import java.time.Instant
import java.time.LocalDate

enum class HabitFrequency {
    Daily,
    Weekly
}

data class Habit(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val frequency: HabitFrequency = HabitFrequency.Daily,
    val targetDaysPerWeek: Int = 7,
    val targetMinutesPerDay: Int? = null,
    val reminderEnabled: Boolean = false,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val createdAt: Instant = Instant.now()
)

data class HabitCompletion(
    val id: Long = 0,
    val habitId: Long,
    val completedDate: LocalDate,
    val completedAt: Instant = Instant.now()
)
