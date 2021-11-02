package com.example.runningtracker.utils

import javax.inject.Inject

class TimeStampProvider
@Inject constructor(){
    fun getMilliseconds(): Long {
        return System.currentTimeMillis()
    }
}
