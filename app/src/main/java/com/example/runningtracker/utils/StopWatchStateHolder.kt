package com.example.runningtracker.utils

import com.example.runningtracker.model.StopWatchState
import javax.inject.Inject

class StopWatchStateHolder
    @Inject constructor(
    private val stopWatchStateCalculator: StopWatchStateCalculator,
    private val elapsedTimeCalculator: ElapsedTimeCalculator,
    private val timestampMillisecondsFormatter: TimestampMillisecondsFormatter
) {
    var currentState: StopWatchState = StopWatchState.Paused(0)

    fun start() {
        currentState = stopWatchStateCalculator.calculateRunningState(currentState)
    }

    fun pause() {
        currentState = stopWatchStateCalculator.calculatePausedState(currentState)
    }

    fun stop() {
        currentState = StopWatchState.Paused(0)
    }

    fun getElapsedTime(): Long {
        val elapsedTime = when (val currentState = currentState) {
            is StopWatchState.Paused -> currentState.elapsedTime
            is StopWatchState.Running -> elapsedTimeCalculator.calculate(currentState)
        }
        return elapsedTime
    }

    fun getStringTimeRepresentation(): String {
        val elapsedTime = when (val currentState = currentState) {
            is StopWatchState.Paused -> currentState.elapsedTime
            is StopWatchState.Running -> elapsedTimeCalculator.calculate(currentState)
        }
        return timestampMillisecondsFormatter.format(elapsedTime)
    }
}