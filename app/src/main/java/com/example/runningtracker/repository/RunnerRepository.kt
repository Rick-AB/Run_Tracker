package com.example.runningtracker.repository

import com.example.runningtracker.model.RunEntry
import com.example.runningtracker.repository.room.RunnerDao
import javax.inject.Inject

class RunnerRepository
@Inject
constructor(
    private val runnerDao: RunnerDao
) {

    suspend fun insertRun(runEntry: RunEntry) {
        runnerDao.insertRun(runEntry)
    }
}