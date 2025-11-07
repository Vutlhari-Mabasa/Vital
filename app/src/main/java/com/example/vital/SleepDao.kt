package com.example.vital.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Insert

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepData(sleepData: SleepData)

    @Query("SELECT * FROM sleep_data ORDER BY startTime DESC")
    fun getAllSleepData(): LiveData<List<SleepData>>

    @Query("SELECT * FROM sleep_data WHERE userId = :userId ORDER BY startTime DESC LIMIT 1")
    fun getLatestSleepData(userId: Int): LiveData<SleepData>
}
