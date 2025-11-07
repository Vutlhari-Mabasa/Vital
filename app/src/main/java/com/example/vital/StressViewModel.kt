package com.example.vital.viewmodel

import androidx.lifecycle.*
import com.example.vital.data.StressData
import com.example.vital.data.StressRepository
import kotlinx.coroutines.launch
import kotlin.random.Random

class StressViewModel(private val repository: StressRepository) : ViewModel() {

    val allStressData = repository.getAllStressData()

    fun getLatestStress(userId: Int) = repository.getLatestStressData(userId)

    fun addStressData(stressData: StressData) = viewModelScope.launch {
        repository.insertStressData(stressData)
    }

    // Simulated stress calculation (based on random HR and HRV values)
    fun analyzeStress(): StressData {
        val heartRate = Random.nextInt(60, 120)
        val hrv = Random.nextDouble(20.0, 100.0)

        // Lower HRV and higher HR → higher stress
        val stressIndex = ((120 - hrv) + (heartRate - 60)) / 2
        val normalizedIndex = stressIndex.coerceIn(0.0, 100.0).toInt()

        val stressLevel = when {
            normalizedIndex < 35 -> "Low"
            normalizedIndex < 70 -> "Moderate"
            else -> "High"
        }

        return StressData(
            userId = 1,
            timestamp = System.currentTimeMillis(),
            heartRate = heartRate,
            hrvScore = hrv,
            stressLevel = stressLevel,
            stressIndex = normalizedIndex
        )
    }
}
