package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class GeoLocation (
    @SerializedName ("latitude") val lat: Double,
    @SerializedName ("longitude") val long: Double
){
    val wsLat: Double
    get() {
        return lat
    }

    val wsLong: Double
    get() {
        return long
    }
}