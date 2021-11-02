package com.example.runningtracker.utils

import javax.inject.Inject

class TimestampMillisecondsFormatter
@Inject constructor(){
    companion object {
        const val DEFAULT_TIME = "00:00:000"
    }

    fun format(timeStamp: Long): String {
        val millisecondsFormatted = (timeStamp % 1000).pad(3)
        val seconds = timeStamp / 1000
        val secondsFormatted = (seconds % 60).pad(2)
        val minutes = seconds / 60
        val minutesFormatted = (minutes % 60).pad(2)
        val hours = minutes / 60
        return if (hours > 1) {
            val hoursFormatted = (minutes / 60).pad(2)
            "$hoursFormatted:$minutesFormatted:$secondsFormatted"
        } else {
            "$minutesFormatted:$secondsFormatted:$millisecondsFormatted"
        }
    }

    private fun Long.pad(desiredLength: Int) = this.toString().padStart(desiredLength, '0')

}