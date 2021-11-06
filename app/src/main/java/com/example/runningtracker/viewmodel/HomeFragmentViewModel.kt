package com.example.runningtracker.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.runningtracker.model.RunEntry
import com.example.runningtracker.repository.RunnerRepository
import com.example.runningtracker.utils.SortTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel
@Inject
constructor(private val repository: RunnerRepository): ViewModel() {

    private val runsSortedByDate = repository.getRunsSortedByDate().asLiveData()
    private val runsSortedByDistance = repository.getRunSortedByDistance().asLiveData()
    private val runsSortedByTime = repository.getRunSortedByTime().asLiveData()
    private val runsSortedByCaloriesBurned = repository.getRunSortedByCaloriesBurned().asLiveData()
    private val runsSortedByAvgSpeed = repository.getRunSortedByAvgSpeed().asLiveData()

    var sortType = SortTypes.DATE

    val runs = MediatorLiveData<List<RunEntry>>()

    init {
        runs.addSource(runsSortedByDate) { result ->
            if (sortType == SortTypes.DATE) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByAvgSpeed) { result ->
            if (sortType == SortTypes.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByDistance) { result ->
            if (sortType == SortTypes.DISTANCE) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByTime) { result ->
            if (sortType == SortTypes.TIME) {
                result?.let { runs.value = it }
            }
        }

        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if (sortType == SortTypes.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
    }

    suspend fun deleteRun(runEntry: RunEntry) {
        repository.deleteRun(runEntry)
    }

    fun sortRuns(sortType: SortTypes) = when (sortType) {
        SortTypes.DATE  -> runsSortedByDate.value?.let { runs.value = it }
        SortTypes.DISTANCE  -> runsSortedByDistance.value?.let { runs.value = it }
        SortTypes.TIME  -> runsSortedByTime.value?.let { runs.value = it }
        SortTypes.AVG_SPEED  -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortTypes.CALORIES_BURNED  -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }
}