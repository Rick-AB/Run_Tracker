package com.example.runningtracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RunEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val imageUrl: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    val distance: Double,
    val averageSpeed: Double
) {
}