package com.example.hooptracker.data.repository

import com.example.hooptracker.data.local.dao.PlayerDao
import com.example.hooptracker.data.local.entity.Player
import kotlinx.coroutines.flow.Flow
/**
 * Controla la gestión de jugadores: guardado, borrado y obtención por equipo o global.
 */

class PlayerRepository(
    private val playerDao: PlayerDao
) {

    fun getPlayersByTeam(teamId: Long): Flow<List<Player>> =
        playerDao.observePlayersByTeam(teamId)

    suspend fun getPlayerById(id: Long): Player? =
        playerDao.getPlayerById(id)

    suspend fun savePlayer(player: Player): Long =
        playerDao.insert(player)

    suspend fun savePlayers(players: List<Player>) =
        playerDao.insertAll(players)

    suspend fun deletePlayer(player: Player) = playerDao.delete(player)

    fun getAllPlayers(): Flow<List<Player>> =
        playerDao.observeAllPlayers()
}
