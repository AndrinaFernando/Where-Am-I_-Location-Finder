package com.example.whereami_locationfindergrp13

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Controller responsible for handling location permissions using the modern Activity Result API.
 * This class encapsulates the logic for checking and requesting permissions.
 *
 * Member 4 Responsibility.
 *
 * @param activity The activity that will host the permission request.
 * @param onPermissionResult Callback invoked when the permission request completes.
 * Returns true if either FINE or COARSE location is granted.
 */
class LocationPermissionController(
    private val activity: ComponentActivity,
    private val onPermissionResult: (Boolean) -> Unit
) {

    /**
     * Register the launcher for requesting multiple permissions.
     * This must be initialized before the Activity reaches the STARTED state.
     *
     * We request both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION to allow
     * the user to choose the level of precision they are comfortable with.
     */
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            // The application can proceed if either precise or approximate location is granted.
            onPermissionResult(fineGranted || coarseGranted)
        }

    /**
     * Checks if at least one location permission (FINE or COARSE) is currently granted.
     * Use ContextCompat.checkSelfPermission for compatibility.
     *
     * @return true if permission is granted, false otherwise.
     */
    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    /**
     * Initiates the location permission request flow.
     * Launches the system permission dialog for both FINE and COARSE location.
     */
    fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Checks whether we should show a rationale for requesting location permissions.
     * Returns true if Android recommends explaining why these permissions are needed.
     */
    fun shouldShowPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
