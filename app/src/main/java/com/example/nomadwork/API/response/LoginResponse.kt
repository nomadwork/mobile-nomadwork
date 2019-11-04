package com.example.nomadwork.API.response

import com.example.nomadwork.models.Token
import com.example.nomadwork.models.User
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: Token,
    @SerializedName("user") val user: User
)