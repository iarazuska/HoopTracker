package com.example.hooptracker.data.local.dao

import androidx.room.*
import com.example.hooptracker.data.local.entity.User
import kotlinx.coroutines.flow.Flow

/*
Controla registro, login, actualización de usuario y estado de sesión.
Permite obtener y observar el usuario logueado y borrar todos los usuarios.
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun observeCurrentUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun getByEmailAndPassword(email: String, password: String): User?

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun clearLoggedIn()

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?
}
