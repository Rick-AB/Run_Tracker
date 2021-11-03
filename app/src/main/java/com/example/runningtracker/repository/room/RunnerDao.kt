package com.example.runningtracker.repository.room

import androidx.room.*
import com.example.runningtracker.model.RunEntry
import kotlinx.coroutines.flow.StateFlow

@Dao
interface RunnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(runEntry: RunEntry)

    @Delete
    suspend fun deleteRun(runEntry: RunEntry)

    @Query("SELECT * FROM RunEntry")
    fun getAllRuns(): StateFlow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY timeStamp DESC")
    fun getAllRunsSortedByDate(): StateFlow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): StateFlow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): StateFlow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY averageSpeedKMH DESC")
    fun getAllRunsSortedBySpeed(): StateFlow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY timeInMilliseconds DESC")
    fun getAllRunsSortedByTotalTime(): StateFlow<List<RunEntry>>

    @Query("SELECT SUM(caloriesBurned) FROM RunEntry")
    fun getTotalCaloriesBurned(): StateFlow<Int>

    @Query("SELECT SUM(distanceInMeters) FROM RunEntry")
    fun getTotalDistance(): StateFlow<Int>

    @Query("SELECT AVG(averageSpeedKMH) FROM RunEntry")
    fun getAvgSpeed(): StateFlow<Float>

    @Query("SELECT SUM(timeInMilliseconds) FROM RunEntry")
    fun getTotalTime(): StateFlow<Long>

}