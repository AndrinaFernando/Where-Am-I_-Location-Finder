package com.example.whereami_locationfindergrp13

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var permissionController: LocationPermissionController
    private lateinit var locationProvider: CurrentLocationProvider

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

        // Handle edge-to-edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        btnGetLocation = findViewById(R.id.btnGetLocation)
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvTimestamp = findViewById(R.id.tvTimestamp)
        tvStatus = findViewById(R.id.tvStatus)
        tvLocationName = findViewById(R.id.tvLocationName)
        progressLocation = findViewById(R.id.progressLocation)

        // Ensure progress is hidden initially
        progressLocation.visibility = View.GONE

        // Initialize controllers
        locationProvider = CurrentLocationProvider(this)
        
        // Initialize permission controller with callback
        permissionController = LocationPermissionController(this) { granted ->
            if (granted) {
                fetchCurrentLocation()
            } else {
                handlePermissionDenied()
            }
        }

        // Set button click listener
        btnGetLocation.setOnClickListener {
            handleLocationButtonClick()
        }
    }

    private fun handleLocationButtonClick() {
        if (permissionController.hasLocationPermission()) {
            fetchCurrentLocation()
        } else if (permissionController.shouldShowPermissionRationale()) {
            tvStatus.text = getString(R.string.permission_rationale)
            permissionController.requestLocationPermission()
        } else {
            tvStatus.text = getString(R.string.status_retrieving)
            permissionController.requestLocationPermission()
        }
    }

    private fun fetchCurrentLocation() {
        if (!isLocationEnabled()) {
            tvStatus.text = getString(R.string.status_location_services_disabled)
            tvLocationName.text = getString(R.string.location_not_available)
            return
        }

        // Update UI state for loading
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

        // Attempt to get address
        fetchAddress(location)

        // Format and display coordinates
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

    private fun fetchAddress(location: Location) {
        try {
            val geocoder = android.location.Geocoder(this, java.util.Locale.getDefault())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    runOnUiThread {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val addressString = address.getAddressLine(0)
                            tvLocationName.text = addressString
                        } else {
                            tvLocationName.text = getString(R.string.status_success)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressString = address.getAddressLine(0)
                    tvLocationName.text = addressString
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
