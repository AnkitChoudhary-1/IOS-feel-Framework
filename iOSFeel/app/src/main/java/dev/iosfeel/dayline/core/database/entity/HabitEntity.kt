package dev.iosfeel.dayline.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.iosfeel.dayline.core.model.Habit
import dev.iosfeel.dayline.core.model.HabitCompletion
import dev.iosfeel.dayline.core.model.HabitFrequency
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
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

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("completedDate")]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val completedDate: LocalDate,
    val completedAt: Instant = Instant.now()
)

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    title = title,
    description = description,
    frequency = frequency,
    targetDaysPerWeek = targetDaysPerWeek,
    targetMinutesPerDay = targetMinutesPerDay,
    reminderEnabled = reminderEnabled,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    createdAt = createdAt
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    title = title,
    description = description,
    frequency = frequency,
    targetDaysPerWeek = targetDaysPerWeek,
    targetMinutesPerDay = targetMinutesPerDay,
    reminderEnabled = reminderEnabled,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    createdAt = createdAt
)

fun HabitCompletionEntity.toDomain(): HabitCompletion = HabitCompletion(
    id = id,
    habitId = habitId,
    completedDate = completedDate,
    completedAt = completedAt
)

fun HabitCompletion.toEntity(): HabitCompletionEntity = HabitCompletionEntity(
    id = id,
    habitId = habitId,
    completedDate = completedDate,
    completedAt = completedAt
)
