package com.example.runningtracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.runningtracker.R
import com.example.runningtracker.ui.MainActivity
import com.example.runningtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracker.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracker.utils.Constants.ACTION_STOP_SERVICE
import com.example.runningtracker.utils.Constants.FASTEST_UPDATE_INTERVAL
import com.example.runningtracker.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningtracker.utils.Constants.NOTIFICATION_ID
import com.example.runningtracker.utils.TimestampMillisecondsFormatter
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this) {
            updateLocationTracking(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startForegroundService()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Log.d("TAG", "onStartCommand: ")
                }
                else -> Log.d("TAG", "onStartCommand: ")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
    }

    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            val request = LocationRequest.create().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = FASTEST_UPDATE_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            if (isTracking.value!!) {
                result.locations.let { locations ->
                    locations.forEach {
                        addPathPoint(it)
                    }
                }
            }
        }

    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val latLng = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(latLng)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    @SuppressLint("ObsoleteSdkInt")
    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_directions_run)
            .setContentTitle("Running App")
            .setContentText(TimestampMillisecondsFormatter.DEFAULT_TIME)
            .setContentIntent(getMainActivityPendingIntent())
            .setAutoCancel(false)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }
}