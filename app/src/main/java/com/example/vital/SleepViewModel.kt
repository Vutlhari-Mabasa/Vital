package com.example.vital.viewmodel

import androidx.lifecycle.*
import com.example.vital.data.SleepData
import com.example.vital.data.SleepRepository
import kotlinx.coroutines.launch

class SleepViewModel(private val repository: SleepRepository) : ViewModel() {

    val allSleepData = repository.getAllSleepData()

    fun addSleepRecord(sleepData: SleepData) = viewModelScope.launch {
        repository.insertSleepData(sleepData)
    }

    fun getLatestSleep(userId: Int) = repository.getLatestSleepData(userId)

    // Simulated analysis — you can later replace this with actual sensor logic
    fun analyzeSleep(startTime: Long, endTime: Long): SleepData {
        val duration = ((endTime - startTime) / (1000 * 60)).toInt()
        val deep = (duration * 0.25).toInt()
        val light = (duration * 0.5).toInt()
        val rem = (duration * 0.25).toInt()
        val quality = (70..100).random()

        return SleepData(
            userId = 1,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = duration,
            deepSleepMinutes = deep,
            lightSleepMinutes = light,
            remSleepMinutes = rem,
            snoringEvents = (0..5).random(),
            bloodOxygenDrops = (0..3).random(),
            sleepQualityScore = quality
        )
    }
}
