package com.example.runningtracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentRunBinding
import com.example.runningtracker.model.RunEntry
import com.example.runningtracker.model.User
import com.example.runningtracker.service.Polyline
import com.example.runningtracker.service.TrackingService
import com.example.runningtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracker.utils.Constants.ACTION_STOP_SERVICE
import com.example.runningtracker.utils.Constants.MAP_TRACKING_ZOOM
import com.example.runningtracker.utils.Constants.POLYLINE_COLOR
import com.example.runningtracker.utils.Constants.POLYLINE_WIDTH
import com.example.runningtracker.viewmodel.RunFragmentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.geometry.Bounds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class RunFragment : Fragment() {
    private lateinit var runBinding: FragmentRunBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var user: User
    private val runFragmentViewModel by viewModels<RunFragmentViewModel>()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        runBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_run, container, false)
        runBinding.viewModel = runFragmentViewModel
        runBinding.lifecycleOwner = viewLifecycleOwner

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            runFragmentViewModel.getUser().collect {
                user = it
            }
        }

        setListeners()
        return runBinding.root
    }

    private fun expandBottomDialog() {
        BottomSheetBehavior.from(runBinding.bottomSheetConstraintLayout).state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setListeners() {

        runBinding.stopWatchStartBtn.setOnClickListener {
            toggleRun()
        }

        runBinding.stopWatchResumeBtn.setOnClickListener {
            resumeRun()
        }

        runBinding.stopWatchFinishRun.setOnClickListener {
            finishRun()
        }
    }

    private fun finishRun() {
        zoomCameraToPointsBounds()
        takeSnapshotAndSave()
        sendCommandToService(ACTION_STOP_SERVICE)
    }

    private fun resumeRun() {
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        runFragmentViewModel.resumeRun()
    }

    private fun toggleRun() {
        if (!isTracking) {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        } else {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireActivity(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }

    private fun hideStartButton() {
        runBinding.stopWatchStartBtn.visibility = View.GONE
    }

    private fun showStartButton() {
        runBinding.stopWatchStartBtn.visibility = View.VISIBLE
    }

    private fun hideButtons() {
        runBinding.stopWatchResumeBtn.visibility = View.GONE
        runBinding.stopWatchFinishRun.visibility = View.GONE
    }

    private fun showButtons() {
        runBinding.stopWatchResumeBtn.visibility = View.VISIBLE
        runBinding.stopWatchFinishRun.visibility = View.VISIBLE
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it!!)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            connectLastTwoLatLng()
            moveCameraToUser()
        }
    }

    private fun zoomCameraToPointsBounds() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (latLng in polyline) {
                bounds.include(latLng)
            }
        }

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                runBinding.map.width,
                runBinding.map.height,
                (runBinding.map.height * 0.05f).toInt()
            )
        )
    }

    private fun takeSnapshotAndSave() {
        googleMap.snapshot { bitmap ->

            var distanceInMeters = 0
            val currentTimeInMillis = runFragmentViewModel.getElapsedTime()
            val avgSpeed = round ((distanceInMeters / 1000f) / (currentTimeInMillis) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * user.weight).toInt()

            for (polyline in pathPoints) {
                distanceInMeters += runFragmentViewModel.calculateTotalDistanceRan(polyline).toInt()
            }

            val run = RunEntry(bitmap, dateTimeStamp, currentTimeInMillis, caloriesBurned, distanceInMeters, avgSpeed)
            runFragmentViewModel.saveRun(run)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking

        if (isTracking) {
            runBinding.stopWatchStartBtn.text = getString(R.string.pause_text)
            hideButtons()
            showStartButton()
        } else {
            runBinding.stopWatchStartBtn.text = getString(R.string.start_text)
            showButtons()
            hideStartButton()
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_TRACKING_ZOOM
                )
            )
        }
    }

    private fun addAllPolylinePoints() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            googleMap.addPolyline(polylineOptions)
        }
    }

    private fun connectLastTwoLatLng() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()

            val polylineOption = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            googleMap.addPolyline(polylineOption)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it

            expandBottomDialog()
            subscribeToObservers()
            addAllPolylinePoints()
        }
    }
}