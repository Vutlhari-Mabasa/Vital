package com.example.vital.data

class SleepRepository(private val sleepDao: SleepDao) {

    fun getAllSleepData() = sleepDao.getAllSleepData()

    fun getLatestSleepData(userId: Int) = sleepDao.getLatestSleepData(userId)

    suspend fun insertSleepData(sleepData: SleepData) {
        sleepDao.insertSleepData(sleepData)
    }
}
