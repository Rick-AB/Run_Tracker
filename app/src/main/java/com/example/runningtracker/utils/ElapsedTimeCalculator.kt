package com.example.runningtracker.utils

import com.example.runningtracker.model.StopWatchState
import javax.inject.Inject

class ElapsedTimeCalculator
@Inject constructor (
    private val timeStampProvider: TimeStampProvider
    ) {

    fun calculate(state: StopWatchState.Running): Long {
        val currentTimeStamp = timeStampProvider.getMilliseconds()
        val timePassedSinceStart = if (currentTimeStamp > state.startTime) {
            currentTimeStamp - state.startTime
        } else {
            0
        }
        return timePassedSinceStart + state.elapsedTime
    }
}