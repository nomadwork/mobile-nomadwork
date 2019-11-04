package com.example.nomadwork.API

import android.content.Context
import com.example.nomadwork.Helpers.PreferencesManager

data class Session (val token: String) {

    companion object {
        private const val preferencesKey = "SessionPreference"
        private var current: Session? = null

        fun get(context: Context?) : Session? {
            if (current == null && context != null) {
                current = PreferencesManager.getPreference(context, preferencesKey, Session::class.java)
            }

            return current
        }
    }

    fun save(context: Context) {
        current = this
        PreferencesManager.setPreference(context, preferencesKey, this)
    }

    fun logout(context: Context) {
        current = null
        PreferencesManager.setPreference(context, preferencesKey, null)
    }

}