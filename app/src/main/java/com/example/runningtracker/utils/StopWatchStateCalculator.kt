package com.example.runningtracker.utils

import com.example.runningtracker.model.StopWatchState
import javax.inject.Inject

class StopWatchStateCalculator
    @Inject constructor(
        private val timeStampProvider: TimeStampProvider,
        private val elapsedTimeCalculator: ElapsedTimeCalculator
) {
    fun calculateRunningState(oldState: StopWatchState): StopWatchState.Running =
        when (oldState) {
            is StopWatchState.Running -> oldState
            is StopWatchState.Paused -> StopWatchState.Running(
                timeStampProvider.getMilliseconds(),
                oldState.elapsedTime
            )
        }

    fun calculatePausedState(oldState: StopWatchState): StopWatchState.Paused =
        when (oldState) {
            is StopWatchState.Paused -> oldState
            is StopWatchState.Running -> {
                val elapsedTime = elapsedTimeCalculator.calculate(oldState)
                StopWatchState.Paused(elapsedTime)
            }
        }
}