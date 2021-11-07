package com.example.runningtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.runningtracker.repository.RunnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel
@Inject
constructor(runnerRepository: RunnerRepository) : ViewModel() {

    val totalRunTime = runnerRepository.getTotalTimeRun().asLiveData()
    val totalDistance = runnerRepository.getTotalDistance().asLiveData()
    val totalCaloriesBurned = runnerRepository.getTotalCaloriesBurned().asLiveData()
    val avgSpeed = runnerRepository.getAvgSpeed().asLiveData()

    val runsSortedByDate = runnerRepository.getRunsSortedByDate().asLiveData()
}