package com.example.nomadwork.API.response

import com.google.gson.annotations.SerializedName

data class WorkStationTableData(
    @SerializedName ("id") val wsId: Int,
    @SerializedName ("name") val wsName: String
)