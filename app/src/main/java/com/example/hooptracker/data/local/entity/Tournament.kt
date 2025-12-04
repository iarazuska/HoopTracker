package com.example.hooptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Almacena la información principal de un torneo: nombre, fechas y descripción opcional.
 */

@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val startDateMillis: Long,
    val endDateMillis: Long?,
    val description: String? = null
)
