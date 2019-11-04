package com.example.nomadwork.Helpers

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.nomadwork.models.User
import com.google.gson.Gson

object PreferencesManager {
    lateinit var user: User
    lateinit var userPhoto: Uri


    fun checkInitialized(): Boolean {
        return this::user.isInitialized
    }

    fun getPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences("NomadWorkPreferences", Context.MODE_PRIVATE)
    }

    fun <T> getPreference(context: Context, key: String, type: Class<T>): T? {
        val preferences = getPreferences(context)
        val json = preferences.getString(key, "")
        if (json == "") {
            return null
        }
        return Gson().fromJson(json, type)
    }

    fun initUserPreferences(user: User) {
        this.user = user
    }

    fun setUserPhotoPreferences(photo: Uri) {
        this.userPhoto = photo
    }

    /**
     * Retorna uma preferencia String ou null caso não exista.
     */
    fun getPreferenceString(context: Context, key: String): String? {
        return getPreferences(context).getString(key, null)
    }

    fun setPreferenceString(context: Context, key: String, value: String) {
        getPreferences(context).edit().putString(key, value).apply()
    }

    fun setPreference(context: Context, key: String, obj: Any?) {
        val preferences = getPreferences(context)
        val json = Gson().toJson(obj)

        val editor = preferences.edit()
        editor.putString(key, json)
        editor.apply()
    }

    fun hasPreference(context: Context, key: String): Boolean {
        val preferences = getPreferences(context)
        return preferences.contains(key)

    }

    fun deletePreference(context: Context, key: String) {
        val preferences = getPreferences(context)
        val editor = preferences.edit()
        editor.remove(key).apply()

    }

}