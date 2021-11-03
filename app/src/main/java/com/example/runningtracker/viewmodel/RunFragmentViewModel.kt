package com.example.runningtracker.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracker.model.RunEntry
import com.example.runningtracker.model.User
import com.example.runningtracker.repository.RunnerRepository
import com.example.runningtracker.repository.datastore.DataStoreManager
import com.example.runningtracker.repository.room.RunnerDatabase
import com.example.runningtracker.service.Polyline
import com.example.runningtracker.utils.StopWatchOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunFragmentViewModel
@Inject constructor(
    private val stopWatchOrchestrator: StopWatchOrchestrator,
    private val dataStoreManager: DataStoreManager,
    private val runnerRepository: RunnerRepository,
) : ViewModel() {

    val stopWatchValue: StateFlow<String> = stopWatchOrchestrator.ticker

    fun startRun() {
        stopWatchOrchestrator.start()
    }

    fun pauseRun() {
        stopWatchOrchestrator.pause()
    }

    fun finishRun() {
        stopWatchOrchestrator.stop()
    }

    fun resumeRun() {
        stopWatchOrchestrator.start()
    }

    fun getUser(): Flow<User> = dataStoreManager.readUserData()

    fun saveRun(runEntry: RunEntry) = viewModelScope.launch {
        runnerRepository.insertRun(runEntry)
    }

    fun getElapsedTime() = stopWatchOrchestrator.getElapsedTime()

    fun calculateTotalDistanceRan(polyline: Polyline): Float {
        var distance = 0f

        for (i in 0..polyline.size - 2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]

            val results = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                results
            )
            distance += results[0]
        }

        return distance
    }
}