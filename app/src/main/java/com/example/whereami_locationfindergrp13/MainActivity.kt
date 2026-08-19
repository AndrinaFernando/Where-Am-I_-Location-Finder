package com.example.whereami_locationfindergrp13

import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

/**
 * MainActivity responsible for integrating all components and managing the UI.
 * This class handles the production-quality permission and GPS flow.
 *
 * Member 5 Responsibility: Integration and UI management.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var permissionController: LocationPermissionController
    private lateinit var locationProvider: CurrentLocationProvider

    // UI Components
    private lateinit var btnGetLocation: MaterialButton
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvLocationName: TextView
    private lateinit var progressLocation: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupWindowInsets()
        initViews()
        initControllers()
    }

    private fun setupWindowInsets() {
        val mainLayout = findViewById<View>(R.id.main)
        val initialPaddingLeft = mainLayout.paddingLeft
        val initialPaddingTop = mainLayout.paddingTop
        val initialPaddingRight = mainLayout.paddingRight
        val initialPaddingBottom = mainLayout.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                initialPaddingLeft + systemBars.left,
                initialPaddingTop + systemBars.top,
                initialPaddingRight + systemBars.right,
                initialPaddingBottom + systemBars.bottom
            )
            insets
        }
    }

    private fun initViews() {
        btnGetLocation = findViewById(R.id.btnGetLocation)
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvTimestamp = findViewById(R.id.tvTimestamp)
        tvStatus = findViewById(R.id.tvStatus)
        tvLocationName = findViewById(R.id.tvLocationName)
        progressLocation = findViewById(R.id.progressLocation)

        progressLocation.visibility = View.GONE

        btnGetLocation.setOnClickListener {
            handleLocationButtonClick()
        }
    }

    private fun initControllers() {
        locationProvider = CurrentLocationProvider(this)
        
        // Initialize permission controller with callback from Member 4's class
        permissionController = LocationPermissionController(this) { granted ->
            if (granted) {
                checkDeviceGpsAndFetch()
            } else {
                handlePermissionDenied()
            }
        }
    }

    /**
     * Handles the "Get My Location" button click using a robust permission flow.
     */
    private fun handleLocationButtonClick() {
        when {
            // 1. Check if we already have permission (Fine or Coarse)
            permissionController.hasLocationPermission() -> {
                checkDeviceGpsAndFetch()
            }

            // 2. Check if the user permanently denied (Don't ask again)
            permissionController.isPermanentlyDenied() -> {
                showPermanentDenialDialog()
            }

            // 3. Check if we should show a rationale (Denied once, but not permanently)
            permissionController.shouldShowPermissionRationale() -> {
                showRationaleDialog()
            }

            // 4. First time requesting or general request
            else -> {
                permissionController.requestLocationPermission()
            }
        }
    }

    /**
     * Checks if System-Level Location (GPS) is enabled.
     * Separate from app permissions.
     */
    private fun checkDeviceGpsAndFetch() {
        if (!isLocationEnabled()) {
            // GPS is off at the system level
            AlertDialog.Builder(this)
                .setTitle(R.string.gps_disabled_title)
                .setMessage(R.string.gps_disabled_message)
                .setPositiveButton(R.string.btn_enable_gps) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(R.string.btn_cancel, null)
                .show()
        } else {
            // Everything is good: Permissions granted AND GPS is on
            fetchCurrentLocation() 
        }
    }

    /**
     * Rationale Dialog: Explains why the app needs permission before the system dialog appears.
     */
    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_rationale_title)
            .setMessage(R.string.permission_rationale_message)
            .setPositiveButton(R.string.btn_ok) { _, _ ->
                permissionController.requestLocationPermission()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    /**
     * Permanent Denial Dialog: Directs user to App Settings since system dialog is blocked.
     */
    private fun showPermanentDenialDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_settings_title)
            .setMessage(R.string.permission_settings_message)
            .setPositiveButton(R.string.btn_settings) { _, _ ->
                permissionController.openAppSettings()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    /**
     * Triggers the location retrieval from the provider (Member 3).
     */
    private fun fetchCurrentLocation() {
        showLoading(true)
        tvStatus.text = getString(R.string.status_retrieving)
        tvLocationName.text = getString(R.string.status_retrieving)

        locationProvider.getCurrentLocation(
            onSuccess = { location ->
                handleLocationSuccess(location)
            },
            onFailure = { exception ->
                handleLocationFailure(exception)
            }
        )
    }

    private fun handleLocationSuccess(location: Location) {
        showLoading(false)
        tvStatus.text = getString(R.string.status_success)

        fetchAddress(location)

        // Display formatted coordinates using Member 5's formatter
        tvLatitude.text = getString(
            R.string.label_latitude_value,
            LocationDisplayFormatter.formatLatitude(location.latitude)
        )
        tvLongitude.text = getString(
            R.string.label_longitude_value,
            LocationDisplayFormatter.formatLongitude(location.longitude)
        )
        tvAccuracy.text = getString(
            R.string.label_accuracy_value,
            LocationDisplayFormatter.formatAccuracy(location.accuracy)
        )
        tvTimestamp.text = getString(
            R.string.label_timestamp_value,
            LocationDisplayFormatter.formatTimestamp(location.time)
        )
    }

    private fun handleLocationFailure(exception: Exception) {
        showLoading(false)
        Log.e("MainActivity", "Location retrieval failed", exception)
        tvStatus.text = getString(R.string.status_error)
        tvLocationName.text = getString(R.string.location_not_available)
    }

    private fun handlePermissionDenied() {
        showLoading(false)
        tvStatus.text = getString(R.string.status_permission_denied)
        tvLocationName.text = getString(R.string.location_not_available)
    }

    /**
     * Helper to retrieve a human-readable address from coordinates.
     */
    private fun fetchAddress(location: Location) {
        try {
            val geocoder = android.location.Geocoder(this, java.util.Locale.getDefault())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    runOnUiThread {
                        if (addresses.isNotEmpty()) {
                            tvLocationName.text = addresses[0].getAddressLine(0)
                        } else {
                            tvLocationName.text = getString(R.string.status_success)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    tvLocationName.text = addresses[0].getAddressLine(0)
                } else {
                    tvLocationName.text = getString(R.string.status_success)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Geocoder failed", e)
            tvLocationName.text = getString(R.string.status_success)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressLocation.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGetLocation.isEnabled = !isLoading
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
