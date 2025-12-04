package com.example.hooptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
Representa un partido de baloncesto con fecha, equipos, marcador, estado, periodo y torneo asociado.
 */

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateMillis: Long,
    val location: String? = null,
    val homeTeamId: Long,
    val awayTeamId: Long,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val status: MatchStatus = MatchStatus.NOT_STARTED,
    val period: Int = 1,
    val tournamentId: Long? = null
)
