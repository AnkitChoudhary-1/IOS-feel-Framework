package dev.iosfeel.dayline.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.iosfeel.dayline.core.model.Expense
import dev.iosfeel.dayline.core.model.ExpenseCategory
import java.time.Instant
import java.time.LocalDateTime

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val currency: String = "₹",
    val title: String,
    val category: ExpenseCategory = ExpenseCategory.Other,
    val dateTime: LocalDateTime,
    val notes: String? = null,
    val createdAt: Instant = Instant.now()
)

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amount = amount,
    currency = currency,
    title = title,
    category = category,
    dateTime = dateTime,
    notes = notes,
    createdAt = createdAt
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amount = amount,
    currency = currency,
    title = title,
    category = category,
    dateTime = dateTime,
    notes = notes,
    createdAt = createdAt
)
