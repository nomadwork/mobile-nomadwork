package com.example.nomadwork.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("dateborn") val age: String,
    @SerializedName("gender") val gender: String
){
    val getName: String
        get() {
            return name
        }

    val getEmail: String
        get() {
            return email
        }

    val getAge: String
        get() {
            return age
        }

    val getGender: String
        get() {
            return gender
        }
}