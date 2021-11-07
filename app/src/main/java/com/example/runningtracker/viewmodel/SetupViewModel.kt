package com.example.runningtracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.runningtracker.model.User
import com.example.runningtracker.repository.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    suspend fun storeData(name: String, weight: String) {
        val user = User(name, weight.toDouble())
        dataStore.saveUserData(user)
    }

    fun validateInput(name: String, weight: String): String? {
        var errorMessage: String? = null

        if (name.isEmpty() || name.isBlank()) {
            errorMessage = "Enter your name"
            return errorMessage
        }

        if (weight.isEmpty() || weight.isBlank()) {
            errorMessage = "Enter your weight"
            return errorMessage
        } else if (weight.toDouble() < 10.0) {
            errorMessage = "Enter weight greater than 10Kg"
            return errorMessage
        }

        return errorMessage
    }
}