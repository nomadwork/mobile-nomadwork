package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class WorkStationDetails(
    @SerializedName ("id") private val wsId: Int,
    @SerializedName ("name") private val wsName: String,
    @SerializedName ("schedule") private val wsSchedule: Schedule,
    @SerializedName ("completeAddress") private val wsAddress: String?,
    @SerializedName ("email") private val wsEmail: String,
    @SerializedName ("phone") private val wsPhone: String,
    @SerializedName ("wifi") private val wifi: Wifi,
    @SerializedName ("noise") private val noise: Noise,
    @SerializedName ("plug") private val plug: Plug,
    @SerializedName ("urlPhoto") private val wsPhotos: List<String>

){
    val workStationId: Int
    get() {
        return wsId
    }

    val workStationName: String
    get() {
        return wsName
    }

    val workStationScheduleOpen: String
    get() {
        return wsSchedule.openWSStation
    }

    val workStationScheduleClose: String
    get() {
        return wsSchedule.closeWSStation
    }

    val workStationAddress: String?
    get() {
        return wsAddress
    }

    val workStationEmail: String
        get() {
            return wsEmail
        }


    val workStationPhone: String
        get() {
            return wsPhone
        }

    val workStationWifi: Wifi
        get() {
            return wifi
        }

    val workStationNoise: Noise
        get() {
            return noise
        }

    val workStationPlug: Plug
        get() {
            return plug
        }

    val workStationPhotos: List<String>
    get() {
        return wsPhotos
    }
}