package com.example.runningtracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.runningtracker.utils.StopWatchOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BottomDialogViewModel
@Inject constructor(
    private val stopWatchOrchestrator: StopWatchOrchestrator
): ViewModel(){

    val stopWatchValue: StateFlow<String> = stopWatchOrchestrator.ticker

    fun start() {
        stopWatchOrchestrator.start()
    }

    fun pause() {
        stopWatchOrchestrator.pause()
    }

    fun finish() {
        stopWatchOrchestrator.stop()
    }

    fun resume() {
        stopWatchOrchestrator.start()
    }
}