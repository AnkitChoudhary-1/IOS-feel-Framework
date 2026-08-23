package dev.iosfeel.dayline.core.database

import androidx.room.TypeConverter
import dev.iosfeel.dayline.core.model.ExpenseCategory
import dev.iosfeel.dayline.core.model.HabitFrequency
import dev.iosfeel.dayline.core.model.TaskPriority
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.format(DateTimeFormatter.ISO_LOCAL_TIME)

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority?): String? = priority?.name

    @TypeConverter
    fun toTaskPriority(value: String?): TaskPriority? = value?.let { TaskPriority.valueOf(it) }

    @TypeConverter
    fun fromHabitFrequency(frequency: HabitFrequency?): String? = frequency?.name

    @TypeConverter
    fun toHabitFrequency(value: String?): HabitFrequency? = value?.let { HabitFrequency.valueOf(it) }

    @TypeConverter
    fun fromExpenseCategory(category: ExpenseCategory?): String? = category?.name

    @TypeConverter
    fun toExpenseCategory(value: String?): ExpenseCategory? = value?.let { ExpenseCategory.valueOf(it) }
}
