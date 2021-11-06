package com.example.runningtracker.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.log
import kotlin.math.round

@AndroidEntryPoint
class RunFragment : Fragment() {
    private lateinit var runBinding: FragmentRunBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var user: User
    private var menu: Menu? = null
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
                Log.d("TAG", "onCreateView: $it")
            }
        }

        setHasOptionsMenu(true)
        setListeners()
        return runBinding.root
    }

    private fun expandBottomDialog() {
        BottomSheetBehavior.from(runBinding.bottomSheetConstraintLayout).state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setListeners() {

        runBinding.stopWatchStartBtn.setOnClickListener {
            showMenu()
            toggleRun()
        }

        runBinding.stopWatchResumeBtn.setOnClickListener {
            resumeRun()
        }

        runBinding.stopWatchFinishRun.setOnClickListener {
            finishRun()
        }
    }

    private fun cancelRun() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Run")
            .setMessage("You will lose the data of this run if you chose to cancel. Are you sure?")
            .setIcon(R.drawable.ic_baseline_delete_24)
            .setPositiveButton("Yes") { _, _ ->
                sendCommandToService(ACTION_STOP_SERVICE)
                googleMap.clear()
                showSnackBar("Run cancelled!")
                findNavController().popBackStack()
            }
            .setNegativeButton("No") {dialogInterface,_ ->
                dialogInterface.cancel()
            }
            .create()

        alertDialog.show()
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            requireActivity().findViewById(R.id.root), message, Snackbar.LENGTH_LONG
        ).show()
    }

    private fun finishRun() {
        zoomCameraToPointsBounds()
        takeSnapshotAndSave()
    }

    private fun resumeRun() {
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }

    private fun toggleRun() {
        if (!isTracking) {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        } else {
            Log.d("TAG", "toggleRun: ${runFragmentViewModel.getElapsedTime()}")
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
            val runEntry = runFragmentViewModel.getRunEntry(bitmap, pathPoints, user.weight)
            viewLifecycleOwner.lifecycleScope.launch {
                runFragmentViewModel.saveRun(runEntry)
                googleMap.clear()
                sendCommandToService(ACTION_STOP_SERVICE)
                showSnackBar("Run saved successfully!")
                findNavController().popBackStack()
            }
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking

        when {
            isTracking -> {
                runBinding.stopWatchStartBtn.text = getString(R.string.pause_text)
                hideButtons()
                showStartButton()
            }
            TrackingService.isFirstRun -> {
                runBinding.stopWatchStartBtn.text = getString(R.string.start_text)
                hideButtons()
                showStartButton()
            }
            else -> {
                runBinding.stopWatchStartBtn.text = getString(R.string.start_text)
                showButtons()
                hideStartButton()
            }
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

    private fun showMenu() {
        this.menu?.getItem(0)?.isVisible = true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        Log.d("TAG", "onPrepareOptionsMenu: CALLED")
        if (runFragmentViewModel.getElapsedTime() > 0L) {
            Log.d("TAG", "onPrepareOptionsMenu: ${this.menu?.getItem(0)}")

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.run_fragment_menu, menu)
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancel_run -> cancelRun()
        }
        return super.onOptionsItemSelected(item)
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