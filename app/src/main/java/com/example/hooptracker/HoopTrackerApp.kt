package com.example.hooptracker

import android.app.Application
import androidx.room.Room
import com.example.hooptracker.data.local.AppDatabase
import com.example.hooptracker.data.repository.AuthRepository
import com.example.hooptracker.data.repository.MatchRepository
import com.example.hooptracker.data.repository.PlayerRepository
import com.example.hooptracker.data.repository.TeamRepository
/**
 * Clase Application que inicializa la base de datos y los repositorios globales de la app.
 */

class HoopTrackerApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "hooptracker.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val matchRepository: MatchRepository by lazy {
        MatchRepository(database.matchDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao())
    }

    val teamRepository: TeamRepository by lazy {
        TeamRepository(database.teamDao())
    }

    val playerRepository: PlayerRepository by lazy {
        PlayerRepository(database.playerDao())
    }
}

