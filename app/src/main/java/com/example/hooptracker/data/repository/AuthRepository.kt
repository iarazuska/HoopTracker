package com.example.hooptracker.data.repository

import com.example.hooptracker.data.local.dao.UserDao
import com.example.hooptracker.data.local.entity.User
import com.example.hooptracker.domain.UserRole
import kotlinx.coroutines.flow.Flow
/**
 * Gestiona el registro, login, sesión y usuario invitado usando UserDao.
 */

class AuthRepository(
    private val userDao: UserDao
) {

    fun observeUser(): Flow<User?> = userDao.observeCurrentUser()

    suspend fun getCurrentUser(): User? =
        userDao.getLoggedInUser()

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): User? {
        userDao.clearLoggedIn()

        val user = User(
            name = name,
            email = email,
            password = password,
            role = role,
            isLoggedIn = true
        )

        val id = userDao.insert(user)
        return user.copy(id = id)
    }

    suspend fun login(email: String, password: String): User? {
        val user = userDao.getByEmailAndPassword(email, password)
        if (user != null) {
            userDao.clearLoggedIn()
            userDao.update(user.copy(isLoggedIn = true))
        }
        return user
    }

    suspend fun loginAsGuest(): User {
        userDao.clearLoggedIn()

        val guestEmail = "guest@local"
        val existing = userDao.findByEmail(guestEmail)

        val guest = if (existing != null) {
            existing.copy(
                role = UserRole.GUEST,
                isLoggedIn = true
            ).also {
                userDao.update(it)
            }
        } else {
            val newGuest = User(
                name = "Invitado",
                email = guestEmail,
                password = "",
                role = UserRole.GUEST,
                favoriteTeamId = null,
                isLoggedIn = true
            )
            val id = userDao.insert(newGuest)
            newGuest.copy(id = id)
        }

        return guest
    }

    suspend fun logout() {
        userDao.clearLoggedIn()
    }
}
