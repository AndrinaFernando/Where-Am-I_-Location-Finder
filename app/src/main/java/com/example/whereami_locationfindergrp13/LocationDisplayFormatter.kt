package com.example.whereami_locationfindergrp13

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility object for formatting Location data for the UI.
 * Member 5 Responsibility.
 */
object LocationDisplayFormatter {

    /**
     * Formats latitude to six decimal places using US locale.
     */
    fun formatLatitude(latitude: Double): String {
        return String.format(Locale.US, "%.6f", latitude)
    }

    /**
     * Formats longitude to six decimal places using US locale.
     */
    fun formatLongitude(longitude: Double): String {
        return String.format(Locale.US, "%.6f", longitude)
    }

    /**
     * Formats accuracy in metres with one decimal place.
     */
    fun formatAccuracy(accuracy: Float): String {
        return String.format(Locale.US, "%.1f m", accuracy)
    }

    /**
     * Formats timestamp into a readable local date and time string.
     * Example: "19 Aug 2026, 10:35:42 AM"
     */
    fun formatTimestamp(timestampMs: Long): String {
        val date = Date(timestampMs)
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}
