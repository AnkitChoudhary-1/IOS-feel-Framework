package dev.iosfeel.dayline.core.database

import dev.iosfeel.dayline.core.model.ExpenseCategory
import dev.iosfeel.dayline.core.model.HabitFrequency
import dev.iosfeel.dayline.core.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun localDate_convertsSymmetrically() {
        val date = LocalDate.of(2026, 8, 23)
        val str = converters.fromLocalDate(date)
        assertEquals("2026-08-23", str)
        assertEquals(date, converters.toLocalDate(str))
        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
    }

    @Test
    fun localTime_convertsSymmetrically() {
        val time = LocalTime.of(14, 30, 0)
        val str = converters.fromLocalTime(time)
        assertEquals("14:30:00", str)
        assertEquals(time, converters.toLocalTime(str))
    }

    @Test
    fun localDateTime_convertsSymmetrically() {
        val dateTime = LocalDateTime.of(2026, 8, 23, 17, 45, 30)
        val str = converters.fromLocalDateTime(dateTime)
        assertEquals(dateTime, converters.toLocalDateTime(str))
    }

    @Test
    fun instant_convertsSymmetrically() {
        val now = Instant.ofEpochMilli(1724400000000L)
        val millis = converters.fromInstant(now)
        assertEquals(1724400000000L, millis)
        assertEquals(now, converters.toInstant(millis))
    }

    @Test
    fun enums_convertSymmetrically() {
        assertEquals("High", converters.fromTaskPriority(TaskPriority.High))
        assertEquals(TaskPriority.High, converters.toTaskPriority("High"))

        assertEquals("Daily", converters.fromHabitFrequency(HabitFrequency.Daily))
        assertEquals(HabitFrequency.Daily, converters.toHabitFrequency("Daily"))

        assertEquals("Food", converters.fromExpenseCategory(ExpenseCategory.Food))
        assertEquals(ExpenseCategory.Food, converters.toExpenseCategory("Food"))
    }
}
