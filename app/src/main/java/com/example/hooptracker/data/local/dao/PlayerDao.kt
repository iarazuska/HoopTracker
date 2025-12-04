package com.example.hooptracker.data.local.dao

import androidx.room.*
import com.example.hooptracker.data.local.entity.Player
import kotlinx.coroutines.flow.Flow

/*
Controla la creación, obtención, observación y borrado de jugadores.
Permite obtener todos los jugadores de un equipo y eliminar jugadores asociados a un equipo.
 */
@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: Player): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<Player>)

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY number")
    fun observePlayersByTeam(teamId: Long): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Long): Player?

    @Query("SELECT * FROM players WHERE teamId = :teamId")
    suspend fun getPlayersByTeam(teamId: Long): List<Player>

    @Delete
    suspend fun delete(player: Player)

    @Query("DELETE FROM players WHERE teamId = :teamId")
    suspend fun deleteByTeamId(teamId: Long)

    @Query("SELECT * FROM players ORDER BY teamId, number")
    fun observeAllPlayers(): Flow<List<Player>>
}
