package dev.iosfeel.dayline.core.model

import java.time.Instant
import java.time.LocalDateTime

enum class ExpenseCategory {
    Food,
    Travel,
    Shopping,
    Bills,
    Education,
    Health,
    Other
}

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val currency: String = "₹",
    val title: String,
    val category: ExpenseCategory = ExpenseCategory.Other,
    val dateTime: LocalDateTime,
    val notes: String? = null,
    val createdAt: Instant = Instant.now()
)
