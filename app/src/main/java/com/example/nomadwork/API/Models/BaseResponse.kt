package com.example.nomadwork.API.Models

import com.example.nomadwork.models.WorkStation
import com.google.gson.annotations.SerializedName

data class BaseResponse<T> (
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: T
)