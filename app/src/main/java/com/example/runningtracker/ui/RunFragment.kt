package com.example.runningtracker.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentBottomDialogBinding
import com.example.runningtracker.databinding.FragmentRunBinding
import com.example.runningtracker.service.Polyline
import com.example.runningtracker.service.TrackingService
import com.example.runningtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracker.utils.Constants.MAP_TRACKING_ZOOM
import com.example.runningtracker.utils.Constants.POLYLINE_COLOR
import com.example.runningtracker.utils.Constants.POLYLINE_WIDTH
import com.example.runningtracker.viewmodel.BottomDialogViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(), OnMapReadyCallback {
    private lateinit var runBinding: FragmentRunBinding
    private lateinit var bottomDialogBinding: FragmentBottomDialogBinding
    private val bottomDialogViewModel by viewModels<BottomDialogViewModel>()
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private lateinit var googleMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //showBottomDialog()
        runBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_run, container, false)
        bottomDialogBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_dialog, container, false)
        runBinding.viewModel = bottomDialogViewModel
        runBinding.lifecycleOwner = viewLifecycleOwner

        setListeners()
        expandBottomDialog()
        return runBinding.root
    }

    private fun expandBottomDialog() {

    }

    private fun setListeners() {

        runBinding.includedBottomDialog.stopWatchStartBtn.setOnClickListener {
            toggleRun()
        }

        runBinding.includedBottomDialog.stopWatchResumeBtn.setOnClickListener {
            resumeRun()
        }

        runBinding.includedBottomDialog.stopWatchFinishRun.setOnClickListener {
            bottomDialogViewModel.finish()
        }
    }

    private fun resumeRun() {
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        bottomDialogViewModel.resume()
        hideButtons()
        showStartButton()
    }

    private fun toggleRun() {
        if (!isTracking) {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            bottomDialogViewModel.start()
            runBinding.includedBottomDialog.stopWatchStartBtn.text =
                getString(R.string.pause_text)

        } else {
            sendCommandToService(ACTION_PAUSE_SERVICE)
            bottomDialogViewModel.pause()
            hideStartButton()
            showButtons()
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireActivity(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }

    private fun hideStartButton() {
        runBinding.includedBottomDialog.stopWatchStartBtn.visibility = View.GONE
    }

    private fun showStartButton() {
        runBinding.includedBottomDialog.stopWatchStartBtn.visibility = View.VISIBLE
    }

    private fun hideButtons() {
        runBinding.includedBottomDialog.stopWatchResumeBtn.visibility = View.GONE
        runBinding.includedBottomDialog.stopWatchFinishRun.visibility = View.GONE
    }

    private fun showButtons() {
        runBinding.includedBottomDialog.stopWatchResumeBtn.visibility = View.VISIBLE
        runBinding.includedBottomDialog.stopWatchFinishRun.visibility = View.VISIBLE
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

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
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
        mapFragment.getMapAsync(this)
        subscribeToObservers()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        addAllPolylinePoints()
    }

}