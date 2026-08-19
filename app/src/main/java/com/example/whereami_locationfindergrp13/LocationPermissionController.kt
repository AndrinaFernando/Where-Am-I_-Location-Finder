package com.example.whereami_locationfindergrp13

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Controller responsible for handling location permissions using the modern Activity Result API.
 * This class encapsulates the logic for checking and requesting permissions, including
 * advanced features like rationale handling and app settings navigation.
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
     * Data class to provide detailed information about the granted permissions.
     */
    data class PermissionStatus(
        val isFineGranted: Boolean,
        val isCoarseGranted: Boolean,
        val anyGranted: Boolean = isFineGranted || isCoarseGranted
    )

    /**
     * Register the launcher for requesting multiple permissions.
     * This must be initialized before the Activity reaches the STARTED state.
     *
     * We request both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION.
     * Android 12+ allows users to grant only "Approximate" location even if "Precise" is requested.
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
     *
     * @return true if permission is granted, false otherwise.
     */
    fun hasLocationPermission(): Boolean {
        return getDetailedPermissionStatus().anyGranted
    }

    /**
     * Returns the current status of both Fine and Coarse permissions.
     */
    fun getDetailedPermissionStatus(): PermissionStatus {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return PermissionStatus(fineGranted, coarseGranted)
    }

    /**
     * Initiates the location permission request flow.
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
     */
    fun shouldShowPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Advanced: Helper to check if location permissions are permanently denied.
     * If this returns true and [hasLocationPermission] is false, the user likely
     * clicked "Don't ask again".
     */
    fun isPermanentlyDenied(): Boolean {
        return !hasLocationPermission() && !shouldShowPermissionRationale()
    }

    /**
     * Advanced: Opens the application details settings page.
     * Useful when permissions are permanently denied and the user needs to enable them manually.
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
}
