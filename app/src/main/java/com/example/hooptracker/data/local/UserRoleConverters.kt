package com.example.hooptracker.data.local

import androidx.room.TypeConverter
import com.example.hooptracker.domain.UserRole

/**
 * Conversor de Room que convierte roles de usuario entre String y UserRole.
 */

class UserRoleConverters {

    @TypeConverter
    fun fromUserRole(role: UserRole?): String? {
        return role?.name
    }

    @TypeConverter
    fun toUserRole(value: String?): UserRole? {
        if (value == null) return null
        return try {
            UserRole.valueOf(value)
        } catch (e: IllegalArgumentException) {
            UserRole.GUEST
        }
    }
}
