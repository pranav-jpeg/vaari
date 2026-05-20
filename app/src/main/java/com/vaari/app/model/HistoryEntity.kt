package com.vaari.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cropName: String,
    val totalWater: Double,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis()
)