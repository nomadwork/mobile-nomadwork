package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class Wifi (
    @SerializedName("rate") private val rate: Int,
    @SerializedName("description") private val description: String
){
    val rateId: Int
        get() {
            return rate
        }

    val descriptionName: String
        get() {
            return description
        }
}