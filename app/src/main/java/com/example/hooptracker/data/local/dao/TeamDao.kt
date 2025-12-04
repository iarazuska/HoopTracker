package com.example.hooptracker.data.local.dao

import androidx.room.*
import com.example.hooptracker.data.local.entity.Team
import kotlinx.coroutines.flow.Flow

/*
Permite insertar, obtener, observar y eliminar equipos.
Devuelve la lista de equipos ordenados alfabéticamente.
 */
@Dao
interface TeamDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: Team): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teams: List<Team>)

    @Query("SELECT * FROM teams ORDER BY name")
    fun observeTeams(): Flow<List<Team>>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: Long): Team?

    @Delete
    suspend fun delete(team: Team)
}
