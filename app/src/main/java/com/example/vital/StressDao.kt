package com.example.vital.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStressData(stressData: StressData)

    @Query("SELECT * FROM stress_data ORDER BY timestamp DESC")
    fun getAllStressData(): LiveData<List<StressData>>

    @Query("SELECT * FROM stress_data WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestStressData(userId: Int): LiveData<StressData>
}
