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

    suspend fun deleteRun(runEntry: RunEntry) {
        runnerDao.deleteRun(runEntry)
    }

    fun getRunsSortedByDate() = runnerDao.getAllRunsSortedByDate()

    fun getRunSortedByDistance() = runnerDao.getAllRunsSortedByDistance()

    fun getRunSortedByTime() = runnerDao.getAllRunsSortedByTotalTime()

    fun getRunSortedByAvgSpeed() = runnerDao.getAllRunsSortedBySpeed()

    fun getRunSortedByCaloriesBurned() = runnerDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalTimeRun() = runnerDao.getTotalTime()

    fun getTotalDistance() = runnerDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runnerDao.getTotalCaloriesBurned()

    fun getAvgSpeed() = runnerDao.getAvgSpeed()

}