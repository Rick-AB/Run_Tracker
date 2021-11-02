package com.example.runningtracker.repository.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.runningtracker.model.RunEntry
import kotlinx.coroutines.flow.StateFlow

@Dao
interface RunnerDao {

    @Insert
    suspend fun insertRun(runEntry: RunEntry)

//    @Query("SELECT * FROM RunEntry")
//    suspend fun getAllRuns(): StateFlow<List<RunEntry>>
}