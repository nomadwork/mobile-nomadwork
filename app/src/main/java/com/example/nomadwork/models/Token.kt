package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("authenticated") val isAuthenticated: Boolean,
    @SerializedName("accessToken") val token: String,
    @SerializedName("created") val createdTime: String,
    @SerializedName("expiration")  val accessTokenTime: String

){
    val getToken: String
        get() {
            return token
        }

    val getIsAuthenticated: Boolean
        get() {
            return isAuthenticated
        }
}