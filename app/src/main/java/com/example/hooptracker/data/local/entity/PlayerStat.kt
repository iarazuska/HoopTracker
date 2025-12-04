package com.example.hooptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Guarda las estadísticas de un jugador en un partido: puntos, rebotes, asistencias y demás acciones.
 */

@Entity(tableName = "player_stats")
data class PlayerStat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val matchId: Long,
    val playerId: Long,
    val isHomeTeam: Boolean,
    val points: Int = 0,
    val rebounds: Int = 0,
    val assists: Int = 0,
    val steals: Int = 0,
    val blocks: Int = 0,
    val turnovers: Int = 0,
    val fouls: Int = 0,
    val minutes: Int = 0
)
