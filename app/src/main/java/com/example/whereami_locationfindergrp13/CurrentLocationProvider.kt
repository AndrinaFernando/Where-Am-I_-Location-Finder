package com.example.whereami_locationfindergrp13

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * Provides a simple API to retrieve the current device location once.
 * This class assumes that location permissions have already been granted.
 */
class CurrentLocationProvider(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /**
     * Retrieves the current location once using HIGH_ACCURACY.
     *
     * @param onSuccess Callback for a successfully retrieved non-null location.
     * @param onFailure Callback for errors or if the location is unavailable.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val cts = CancellationTokenSource()

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onFailure(Exception("Current location is unavailable. Ensure location services are enabled and try again."))
                }
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        } catch (e: SecurityException) {
            // Defensive catch in case permissions were revoked after the check
            onFailure(Exception("Location permission is missing or has been revoked.", e))
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
