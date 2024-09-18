package com.liburngoding.androiddevtest2.ui.theme.main.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destination_data : String,
    val source_data : String,
    val income_amount : String,
    val description : String,
    val category : String,
    val files : String,
    val created_at: Long = System.currentTimeMillis()
)

