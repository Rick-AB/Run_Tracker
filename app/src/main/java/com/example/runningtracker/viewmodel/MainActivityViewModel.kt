package com.example.runningtracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.runningtracker.model.User
import com.example.runningtracker.repository.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

     suspend fun checkUser(): Flow<User> = dataStoreManager.readUserData()

}