package com.example.runningtracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.runningtracker.R
import com.example.runningtracker.databinding.ActivityMainBinding
import com.example.runningtracker.model.User
import com.example.runningtracker.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningtracker.utils.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.example.runningtracker.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    private lateinit var navGraph: NavGraph
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var user: User
    private val viewModel by viewModels<MainActivityViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment)
        navController = navHostFragment.navController
        val inflater = navController.navInflater
        navGraph = inflater.inflate(R.navigation.nav_graph)

        binding.bottomNav.setupWithNavController(navController)
        NavigationUI.setupWithNavController(binding.bottomNav, navHostFragment.navController)

        lifecycleScope.launchWhenCreated {
            viewModel.checkUser().collect {
                user = it
                setUpInitialDestination(it)
            }
        }

        setDestinationChangeListener()
    }

    private fun navigateToRunFragmentWithNotification(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.runFragment)
        }
    }

    private fun setUpInitialDestination(user: User) {
        if (user.name == "") {
            navGraph.startDestination = R.id.setupFragment
        } else {
            navGraph.startDestination = R.id.homeFragment
            setAppBarTitle(user.name)
            showBottomNavBar()
        }
        navController.graph = navGraph
        navigateToRunFragmentWithNotification(intent)
    }

    private fun setDestinationChangeListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.runFragment) {
                hideBottomNavBar()
            }

            if (destination.id == R.id.homeFragment) {
                checkLocationPermission()
                showBottomNavBar()
            }
        }

    }

    private fun showBottomNavBar() {
        binding.bottomNav.visibility = View.VISIBLE

    }

    private fun hideBottomNavBar() {
        binding.bottomNav.visibility = View.GONE
    }

    private fun setAppBarTitle(name: String) {
        val appBar = supportActionBar
        appBar?.title = "Let's go, $name!"
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            if (shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showAlertDialog()
            } else {
                requestPermission()
            }

        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs the Location permission, please accept to use location functionality")
            .setPositiveButton(
                "OK"
            ) { _, _ ->
                requestPermission()
            }
            .create()
            .show()
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()

                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            ),
                        )
                    } else {
                        showErrorDialog()
                    }
                }
            } else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission denied!")
            .setMessage("You won't be able to use this app without granting permission")
            .setPositiveButton(
                "OK"
            ) { _, _ ->
                finish()
            }
            .create()
            .show()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("TAG", "onNewIntent: THIS")
        navigateToRunFragmentWithNotification(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::user.isInitialized && user.name != "") {
            checkLocationPermission()
        }
    }
}