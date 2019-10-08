package com.example.nomadwork.API.request

import com.google.gson.annotations.SerializedName

data class PasswordRequest(
    @SerializedName("passwordEncode") val password: String
){
    fun parseToString(): String {
        val test = password.length

        password.removeRange((test-2), test-1)

        return password
    }
}