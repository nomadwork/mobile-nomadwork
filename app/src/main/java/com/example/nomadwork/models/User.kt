package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("nome") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("idade") val idade: Int,
    @SerializedName("sexo") val sexo: String
){

}