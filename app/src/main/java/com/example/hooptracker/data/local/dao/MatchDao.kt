package com.example.hooptracker.data.local.dao

import androidx.room.*
import com.example.hooptracker.data.local.entity.Match
import kotlinx.coroutines.flow.Flow

/*
Este DAO se encarga de crear, actualizar, obtener, observar en tiempo real y eliminar partidos.
También permite contar partidos por equipo y listar los partidos de un torneo.
 */
@Dao
interface MatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: Match): Long

    @Update
    suspend fun update(match: Match)

    @Query("SELECT * FROM matches ORDER BY dateMillis DESC")
    fun observeMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): Match?

    @Delete
    suspend fun delete(match: Match)

    @Query(
        """SELECT COUNT(*) FROM matches 
        WHERE homeTeamId = :teamId OR awayTeamId = :teamId"""
    )
    suspend fun countMatchesForTeam(teamId: Long): Int

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY dateMillis")
    fun observeMatchesByTournament(tournamentId: Long): Flow<List<Match>>
}
