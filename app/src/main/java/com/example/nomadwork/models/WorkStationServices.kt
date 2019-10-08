package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class WorkStationServices (
    @SerializedName ("quantity") private val quantity: Int,
    @SerializedName ("quality") private val quality: Int,
    @SerializedName ("service") private val service: Int
){
    val serviceQuantity: Int
    get() {
        return quantity
    }

    val serviceQuality: Int
    get() {
        return quality
    }

    val serviceId: Int
    get() {
        return service
    }
}