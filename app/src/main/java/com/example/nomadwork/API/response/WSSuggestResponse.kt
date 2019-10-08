package com.example.nomadwork.API.response

import com.example.nomadwork.models.Noise
import com.example.nomadwork.models.Plug
import com.example.nomadwork.models.Schedule
import com.example.nomadwork.models.Wifi
import com.google.gson.annotations.SerializedName

data class WSSuggestResponse (
    @SerializedName("name") val wsName: String,
    @SerializedName("phone") val wsPhone: String,
    @SerializedName("email") val wsEmail: String,
    @SerializedName("latitude") val wsLatitude: Double,
    @SerializedName("longitude") val wsLong: Double,
    @SerializedName("schedule") val wsSchedule: Schedule,
    @SerializedName("wifi") val wsWifi: Wifi,
    @SerializedName("noise") val wsNoise: Noise,
    @SerializedName("plug") val wsPlug: Plug
)