package com.example.runningtracker.model

sealed class StopWatchState {
    data class Paused(val elapsedTime: Long): StopWatchState()
    data class Running(val startTime: Long, val elapsedTime: Long): StopWatchState()
}
