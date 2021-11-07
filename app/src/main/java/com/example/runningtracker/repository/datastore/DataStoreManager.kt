package com.example.runningtracker.repository.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.runningtracker.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.userDataStore by preferencesDataStore(name = "USER_DATASTORE")

class DataStoreManager @Inject constructor(
     @ApplicationContext val context: Context
) {
    companion object {
        val NAME = stringPreferencesKey("User's name")
        val WEIGHT = doublePreferencesKey("User's weight")
    }

    suspend fun saveUserData(user: User) {
        context.userDataStore.edit { preferences ->
            preferences[NAME] = user.name
            preferences[WEIGHT] = user.weight
        }
    }

    fun readUserData() = context.userDataStore.data.map {
        User(
            name = it[NAME] ?: "",
            weight = it[WEIGHT] ?: 0.0
        )
    }


}