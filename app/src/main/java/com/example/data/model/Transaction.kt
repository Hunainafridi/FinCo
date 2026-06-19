package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String = "PKR",
    val dateMillis: Long,
    val description: String,
    val categoryId: String,
    val source: String, // 'manual', 'sms', 'voice'
    val isIncome: Boolean,
    val mlConfidence: Double? = null,
    val rawSmsSender: String? = null,
    val isPending: Boolean = false
)
