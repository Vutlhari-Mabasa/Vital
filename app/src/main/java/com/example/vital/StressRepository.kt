package com.example.vital.data

class StressRepository(private val stressDao: StressDao) {

    fun getAllStressData() = stressDao.getAllStressData()

    fun getLatestStressData(userId: Int) = stressDao.getLatestStressData(userId)

    suspend fun insertStressData(stressData: StressData) {
        stressDao.insertStressData(stressData)
    }
}
