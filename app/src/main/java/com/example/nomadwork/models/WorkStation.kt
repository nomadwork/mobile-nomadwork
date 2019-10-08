package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class WorkStation(
    @SerializedName ("id") private val wsId: Int,
    @SerializedName ("name") private val wsName: String,
    @SerializedName ("geolocation") private val wsLocation: GeoLocation
){
    val workStationId: Int
    get() {
        return wsId
    }

    val workStationName: String
    get() {
        return wsName
    }

    val workStationLocation: GeoLocation
    get() {
        return wsLocation
    }
}