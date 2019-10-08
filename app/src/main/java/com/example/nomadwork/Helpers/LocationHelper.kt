package com.example.nomadwork.Helpers

import android.location.Location
import android.util.Log
import com.google.gson.annotations.SerializedName


object LocationHelper {
    private lateinit var lastLocation: Location

    fun updateLocation(location: Location) {
        this.lastLocation = location
        Log.i("Location", "Lat: ${location.latitude} Long: ${location.longitude}")

    }

    fun getUserLocation(): UserLocation? {
        if (this::lastLocation.isInitialized) {
            return UserLocation(lastLocation.latitude, lastLocation.longitude)
        }
        return null
    }

    fun distanceCurrentLocationAndNewLocation(lat1: Double, long1: Double, lat2: Double, long2: Double): Float {
        val start = Location("")
        start.latitude = lat1
        start.longitude = long1

        val finish = Location("")
        finish.latitude = lat2
        finish.longitude = long2

        return start.distanceTo(finish)
    }
}

data class UserLocation(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double
)