package com.example.runningtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracker.model.User
import com.example.runningtracker.repository.RunnerRepository
import com.example.runningtracker.repository.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject
constructor(private val dataStoreManager: DataStoreManager) : ViewModel() {

    fun getUser() = dataStoreManager.readUserData()

    fun saveChanges (name: String, weight: String): Boolean {
        val isValid = validate(name, weight)
        if (!isValid) {
            return false
        }

        val user = User(name, weight.toDouble())
        viewModelScope.launch {
            dataStoreManager.saveUserData(user)
        }
        return true
    }

    private fun validate (name: String, weight: String): Boolean {
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        return true
    }
}