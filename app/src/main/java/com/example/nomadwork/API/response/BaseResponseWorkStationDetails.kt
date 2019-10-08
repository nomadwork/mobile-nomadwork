package com.example.nomadwork.API.response

import com.example.nomadwork.models.WorkStationDetails
import com.google.gson.annotations.SerializedName

data class BaseResponseWorkStationDetails<T> (
    @SerializedName ("message") val message: String,
    @SerializedName ("result") val workStationDetails: WorkStationDetails
)