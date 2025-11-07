package com.example.vital.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_data")
data class SleepData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val deepSleepMinutes: Int,
    val lightSleepMinutes: Int,
    val remSleepMinutes: Int,
    val snoringEvents: Int,
    val bloodOxygenDrops: Int,
    val sleepQualityScore: Int // 0–100
)
