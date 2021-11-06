package com.example.runningtracker.repository.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runningtracker.model.RunEntry

@Database(entities = [RunEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RunnerDatabase: RoomDatabase() {
    abstract fun runnerDao(): RunnerDao
}