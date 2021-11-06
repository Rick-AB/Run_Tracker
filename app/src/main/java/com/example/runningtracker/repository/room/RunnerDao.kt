package com.example.runningtracker.repository.room

import androidx.room.*
import com.example.runningtracker.model.RunEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface RunnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(runEntry: RunEntry)

    @Delete
    suspend fun deleteRun(runEntry: RunEntry)

    @Query("SELECT * FROM RunEntry")
    fun getAllRuns(): Flow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY timeStamp DESC")
    fun getAllRunsSortedByDate(): Flow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): Flow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): Flow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY averageSpeedKMH DESC")
    fun getAllRunsSortedBySpeed(): Flow<List<RunEntry>>

    @Query("SELECT * FROM RunEntry ORDER BY timeInMilliseconds DESC")
    fun getAllRunsSortedByTotalTime(): Flow<List<RunEntry>>

    @Query("SELECT SUM(caloriesBurned) FROM RunEntry")
    fun getTotalCaloriesBurned(): Flow<Int>

    @Query("SELECT SUM(distanceInMeters) FROM RunEntry")
    fun getTotalDistance(): Flow<Int>

    @Query("SELECT AVG(averageSpeedKMH) FROM RunEntry")
    fun getAvgSpeed(): Flow<Float>

    @Query("SELECT SUM(timeInMilliseconds) FROM RunEntry")
    fun getTotalTime(): Flow<Long>

}