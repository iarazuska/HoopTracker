package com.example.hooptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Almacena la información básica de un jugador: nombre, dorsal, posición y equipo al que pertenece.
 */

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val number: Int,
    val position: String,
    val teamId: Long
)
