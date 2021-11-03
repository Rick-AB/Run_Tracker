package com.example.runningtracker.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RunEntry(
    var image: Bitmap? = null,
    var timeStamp: Long = 0L,
    var timeInMilliseconds: Long = 0L,
    var caloriesBurned: Int = 0,
    var distanceInMeters: Int = 0,
    var averageSpeedKMH: Float = 0f
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}