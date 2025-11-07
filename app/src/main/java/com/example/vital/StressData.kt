package com.example.vital.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stress_data")
data class StressData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val timestamp: Long,
    val heartRate: Int,              // beats per minute
    val hrvScore: Double,            // simulated HRV (ms)
    val stressLevel: String,         // "Low", "Moderate", "High"
    val stressIndex: Int             // 0–100
)
