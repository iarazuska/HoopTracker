package com.example.hooptracker.data.local.dao

import androidx.room.*
import com.example.hooptracker.data.local.entity.PlayerStat
import kotlinx.coroutines.flow.Flow

/*
Gestiona todas las estadísticas de los jugadores en los partidos: inserción, consulta y borrado.
Incluye funciones para calcular totales de carrera, obtener máximos anotadores y observar stats en tiempo real.
 */
@Dao
interface PlayerStatDao {

    data class PlayerTotals(
        val totalPoints: Int?,
        val totalRebounds: Int?,
        val totalAssists: Int?,
        val totalSteals: Int?,
        val totalTurnovers: Int?,
        val totalBlocks: Int?,
        val totalFouls: Int?,
        val totalMinutes: Int?
    )

    data class PlayerPointsLeader(
        val playerId: Long,
        val totalPoints: Int
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: PlayerStat): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<PlayerStat>)

    @Query("SELECT * FROM player_stats WHERE matchId = :matchId")
    fun observeByMatch(matchId: Long): Flow<List<PlayerStat>>

    @Query(
        "SELECT * FROM player_stats " +
                "WHERE matchId = :matchId AND playerId = :playerId LIMIT 1"
    )
    suspend fun getStatForPlayer(matchId: Long, playerId: Long): PlayerStat?

    @Query("SELECT * FROM player_stats WHERE matchId = :matchId")
    suspend fun getByMatch(matchId: Long): List<PlayerStat>

    @Query("SELECT * FROM player_stats WHERE playerId = :playerId ORDER BY matchId")
    suspend fun getStatsForPlayer(playerId: Long): List<PlayerStat>

    @Query(
        """
        SELECT 
            SUM(points) as totalPoints,
            SUM(rebounds) as totalRebounds,
            SUM(assists) as totalAssists,
            SUM(steals) as totalSteals,
            SUM(turnovers) as totalTurnovers,
            SUM(blocks) as totalBlocks,
            SUM(fouls) as totalFouls,
            SUM(minutes) as totalMinutes
        FROM player_stats
        WHERE playerId = :playerId
        """
    )
    suspend fun getPlayerTotals(playerId: Long): PlayerTotals?

    @Query(
        """
        SELECT playerId, SUM(points) AS totalPoints
        FROM player_stats
        GROUP BY playerId
        ORDER BY totalPoints DESC
        LIMIT :limit
        """
    )
    suspend fun getTopScorers(limit: Int): List<PlayerPointsLeader>

    @Query("SELECT * FROM player_stats WHERE matchId = :matchId")
    fun observeStatsByMatch(matchId: Long): Flow<List<PlayerStat>>

    @Query("DELETE FROM player_stats WHERE matchId = :matchId")
    suspend fun deleteStatsByMatch(matchId: Long)

    @Delete
    suspend fun delete(stat: PlayerStat)

    @Query(
        """
        SELECT playerId, SUM(points) AS totalPoints
        FROM player_stats
        GROUP BY playerId
        ORDER BY totalPoints DESC
        LIMIT :limit
        """
    )
    fun observeTopScorers(limit: Int): Flow<List<PlayerPointsLeader>>
}
