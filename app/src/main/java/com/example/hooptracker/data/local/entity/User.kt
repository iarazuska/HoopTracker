package com.example.hooptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hooptracker.domain.UserRole

/**
 * Define un usuario del sistema con sus datos, rol, equipo favorito y estado de sesión.
 */

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val email: String,
    val password: String = "",
    val role: UserRole = UserRole.COACH,
    val favoriteTeamId: Long? = null,
    val isLoggedIn: Boolean = false
)
