package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class Schedule (
    @SerializedName ("open") private val open: String,
    @SerializedName ("close") private val close: String
){
    val openWSStation: String
    get() {
        return open
    }

    val closeWSStation: String
    get() {
        return close
    }
}