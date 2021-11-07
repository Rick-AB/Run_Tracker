package com.example.runningtracker.viewmodel

import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.runningtracker.model.RunEntry
import com.example.runningtracker.model.User
import com.example.runningtracker.repository.RunnerRepository
import com.example.runningtracker.repository.datastore.DataStoreManager
import com.example.runningtracker.service.Polyline
import com.example.runningtracker.service.Polylines
import com.example.runningtracker.utils.StopWatchOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class RunFragmentViewModel
@Inject constructor(
    private val stopWatchOrchestrator: StopWatchOrchestrator,
    private val dataStoreManager: DataStoreManager,
    private val runnerRepository: RunnerRepository,
) : ViewModel() {

    val stopWatchValue: StateFlow<String> = stopWatchOrchestrator.ticker

    fun getUser(): Flow<User> = dataStoreManager.readUserData()

    suspend fun saveRun(runEntry: RunEntry) {
        runnerRepository.insertRun(runEntry)
    }

    fun getElapsedTime() = stopWatchOrchestrator.getElapsedTime()

    private fun calculateTotalDistanceRan(polyline: Polyline): Float {
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

    private fun getSpeedInKMH(avgSpeedMetersPerSec: Float): Float {
        val df = DecimalFormat("#.#")
        return df.format(avgSpeedMetersPerSec * 3.6f).toFloat()
    }

    fun getRunEntry(bitmap: Bitmap?, pathPoints: Polylines, weight: Double): RunEntry {
        var distanceInMeters = 0

        for (polyline in pathPoints) {
            distanceInMeters += calculateTotalDistanceRan(polyline).toInt()
        }

        val currentTimeInMillis = getElapsedTime()
        val avgSpeedMetersPerSec = (distanceInMeters) / (currentTimeInMillis / 1000f)
        val avgSpeedKHM = getSpeedInKMH(avgSpeedMetersPerSec)
        val dateTimeStamp = Calendar.getInstance().timeInMillis
        val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

        return RunEntry(bitmap, dateTimeStamp, currentTimeInMillis, caloriesBurned, distanceInMeters, avgSpeedKHM)
    }
}