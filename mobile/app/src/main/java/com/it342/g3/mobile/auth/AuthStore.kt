package com.it342.g3.mobile.auth

import android.content.Context
import com.it342.g3.mobile.api.AuthData

object AuthStore {
    private const val PREFS_NAME = "auth"

    fun getToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("token", "") ?: ""
    }

    fun getAuthHeader(context: Context): String {
        val token = getToken(context)
        return if (token.isNotBlank()) "Bearer $token" else ""
    }

    fun getDisplayName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "") ?: ""
        val username = prefs.getString("username", "") ?: ""
        return if (fullName.isNotBlank()) fullName else username
    }

    fun getEmail(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("email", "") ?: ""
    }

    fun saveAuth(context: Context, auth: AuthData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("token", auth.token ?: "")
            .putString("username", auth.username ?: "")
            .putString("email", auth.email ?: "")
            .putString("fullName", auth.fullName ?: "")
            .apply()
    }

    fun updateProfile(context: Context, fullName: String?, username: String?, email: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (fullName != null) {
            editor.putString("fullName", fullName)
        }
        if (username != null) {
            editor.putString("username", username)
        }
        if (email != null) {
            editor.putString("email", email)
        }
        editor.apply()
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
