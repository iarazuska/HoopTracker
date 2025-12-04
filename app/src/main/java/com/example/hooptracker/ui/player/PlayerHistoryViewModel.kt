package com.example.hooptracker.ui.player

import androidx.lifecycle.*
import com.example.hooptracker.data.local.dao.PlayerDao
import com.example.hooptracker.data.local.dao.PlayerStatDao
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.PlayerStat
import com.example.hooptracker.data.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
/**
 * Carga estadísticas históricas del jugador y prepara datos para la gráfica y los totales.
 */

data class PlayerHistoryUiState(
    val playerName: String = "",
    val matches: List<Match> = emptyList(),
    val pointsPerMatch: List<Int> = emptyList(),
    val avgPoints: Double = 0.0,
    val totalPoints: Int = 0,
    val totalRebounds: Int = 0,
    val totalAssists: Int = 0,
    val totalSteals: Int = 0,
    val totalTurnovers: Int = 0
)

class PlayerHistoryViewModel(
    private val playerId: Long,
    private val playerDao: PlayerDao,
    private val playerStatDao: PlayerStatDao,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(PlayerHistoryUiState())
    val uiState: LiveData<PlayerHistoryUiState> = _uiState

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val player = playerDao.getPlayerById(playerId) ?: return@launch

            val stats: List<PlayerStat> = playerStatDao.getStatsForPlayer(playerId)

            val matches = mutableListOf<Match>()
            val points = mutableListOf<Int>()

            for (stat in stats) {
                val match = matchRepository.getMatchById(stat.matchId)
                if (match != null) {
                    matches.add(match)
                    points.add(stat.points)
                }
            }

            val avg = if (points.isNotEmpty()) points.average() else 0.0

            val totalPoints = stats.sumOf { it.points }
            val totalRebounds = stats.sumOf { it.rebounds }
            val totalAssists = stats.sumOf { it.assists }
            val totalSteals = stats.sumOf { it.steals }
            val totalTurnovers = stats.sumOf { it.turnovers }

            _uiState.postValue(
                PlayerHistoryUiState(
                    playerName = player.name,
                    matches = matches,
                    pointsPerMatch = points,
                    avgPoints = avg,
                    totalPoints = totalPoints,
                    totalRebounds = totalRebounds,
                    totalAssists = totalAssists,
                    totalSteals = totalSteals,
                    totalTurnovers = totalTurnovers
                )
            )
        }
    }

    class Factory(
        private val playerId: Long,
        private val playerDao: PlayerDao,
        private val playerStatDao: PlayerStatDao,
        private val matchRepository: MatchRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerHistoryViewModel(
                playerId,
                playerDao,
                playerStatDao,
                matchRepository
            ) as T
        }
    }
}
