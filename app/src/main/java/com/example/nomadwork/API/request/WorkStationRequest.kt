package com.example.nomadwork.API.request

import com.google.gson.annotations.SerializedName

data class WorkStationRequest(
    @SerializedName ("latitude") val lat: Double,
    @SerializedName ("longitude") val long: Double
)