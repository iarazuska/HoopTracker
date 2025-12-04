package com.example.hooptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un equipo con su nombre y datos opcionales como ciudad y color identificativo.
 */

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val city: String? = null,
    val color: String? = null
)
