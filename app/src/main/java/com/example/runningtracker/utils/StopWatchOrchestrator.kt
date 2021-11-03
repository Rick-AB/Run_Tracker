package com.example.runningtracker.utils

import com.example.runningtracker.di.DefaultDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class StopWatchOrchestrator
@Inject constructor(
    private val stopWatchStateHolder: StopWatchStateHolder,
    private val scope: CoroutineScope
) {
    private var job: Job? = null
    private val mutableTicker = MutableStateFlow(TimestampMillisecondsFormatter.DEFAULT_TIME)
    val ticker: StateFlow<String> = mutableTicker

    fun start() {
        if (job == null) startJob()
        stopWatchStateHolder.start()
    }

    private fun startJob() {
        scope.launch {
            while (isActive) {
                mutableTicker.value = stopWatchStateHolder.getStringTimeRepresentation()
                delay(20)
            }
        }
    }

    fun getElapsedTime() = stopWatchStateHolder.getElapsedTime()

    fun pause() {
        stopWatchStateHolder.pause()
        stopJob()
    }

    fun stop() {
        stopWatchStateHolder.stop()
        stopJob()
        clearValue()
    }

    private fun stopJob() {
        scope.coroutineContext.cancelChildren()
        job = null
    }

    private fun clearValue() {
        mutableTicker.value = TimestampMillisecondsFormatter.DEFAULT_TIME
    }
}