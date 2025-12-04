package com.example.hooptracker.data.session

import android.content.Context
import com.example.hooptracker.domain.UserRole
/**
 * Gestiona el rol del usuario en la sesión usando SharedPreferences.
 */

object UserSession {

    private const val PREFS_NAME = "user_session"
    private const val KEY_ROLE = "role"

    fun saveRole(context: Context, role: UserRole) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ROLE, role.name)
            .apply()
    }

    fun getRole(context: Context): UserRole? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getString(KEY_ROLE, null) ?: return null

        return try {
            UserRole.valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
