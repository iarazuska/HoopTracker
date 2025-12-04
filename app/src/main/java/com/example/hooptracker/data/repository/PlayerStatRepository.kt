package com.example.hooptracker.data.repository

import com.example.hooptracker.data.local.dao.PlayerStatDao
import com.example.hooptracker.data.local.entity.PlayerStat
import kotlinx.coroutines.flow.Flow
/**
 * Gestiona las estadísticas de los jugadores, incluyendo consulta, guardado y máximos anotadores.
 */

class PlayerStatRepository(
    private val playerStatDao: PlayerStatDao
) {

    fun getStatsByMatch(matchId: Long): Flow<List<PlayerStat>> =
        playerStatDao.observeStatsByMatch(matchId)

    suspend fun getStatsForPlayer(playerId: Long): List<PlayerStat> =
        playerStatDao.getStatsForPlayer(playerId)

    suspend fun savePlayerStat(stat: PlayerStat) =
        playerStatDao.insert(stat)

    suspend fun deleteStat(stat: PlayerStat) =
        playerStatDao.delete(stat)

    suspend fun deleteStatsByMatch(matchId: Long) =
        playerStatDao.deleteStatsByMatch(matchId)

    suspend fun getTopScorers(limit: Int) =
        playerStatDao.getTopScorers(limit)
}
