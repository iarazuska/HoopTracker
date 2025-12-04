package com.example.hooptracker.data.local.dao

import androidx.room.*
import com.example.hooptracker.data.local.entity.Tournament
import kotlinx.coroutines.flow.Flow

/*
Crea, actualiza, obtiene y elimina torneos.
Ofrece una lista de torneos en tiempo real ordenada por fecha.
 */
@Dao
interface TournamentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tournament: Tournament): Long

    @Update
    suspend fun update(tournament: Tournament)

    @Delete
    suspend fun delete(tournament: Tournament)

    @Query("SELECT * FROM tournaments ORDER BY startDateMillis DESC")
    fun observeTournaments(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getById(id: Long): Tournament?

    @Delete
    suspend fun deleteTournament(tournament: Tournament)
}
