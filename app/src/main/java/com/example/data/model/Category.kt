package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean,
    val isIncome: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category("grocery", "Groceries", "shopping_cart", "#4CAF50", true, false),
            Category("food", "Food & Dining", "restaurant", "#FF9800", true, false),
            Category("transport", "Transport", "directions_car", "#2196F3", true, false),
            Category("bills", "Bills & Utilities", "receipt_long", "#E91E63", true, false),
            Category("education", "Education", "school", "#9C27B0", true, false),
            Category("salary", "Salary", "payments", "#009688", true, true),
            Category("other_income", "Other Income", "add_card", "#8BC34A", true, true),
            Category("uncategorized", "Uncategorized", "help", "#9E9E9E", true, false)
        )
    }
}
