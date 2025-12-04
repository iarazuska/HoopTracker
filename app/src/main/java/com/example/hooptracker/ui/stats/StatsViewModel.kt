package com.example.hooptracker.ui.stats

import androidx.lifecycle.*
import com.example.hooptracker.data.local.dao.PlayerDao
import com.example.hooptracker.data.local.dao.PlayerStatDao
import com.example.hooptracker.data.local.dao.TeamDao
import kotlinx.coroutines.flow.map
/**
 * Obtiene los máximos anotadores desde la BD y prepara los datos para mostrarlos en la gráfica.
 */

data class ScorerUi(
    val playerId: Long,
    val playerName: String,
    val teamName: String,
    val totalPoints: Int
)

data class StatsUiState(
    val topScorers: List<ScorerUi> = emptyList()
)

class StatsViewModel(
    private val playerStatDao: PlayerStatDao,
    private val playerDao: PlayerDao,
    private val teamDao: TeamDao
) : ViewModel() {

    val uiState: LiveData<StatsUiState> =
        playerStatDao.observeTopScorers(limit = 10)
            .map { leaders ->
                val items = leaders.mapNotNull { leader ->
                    val player = playerDao.getPlayerById(leader.playerId) ?: return@mapNotNull null
                    val team = teamDao.getTeamById(player.teamId)
                    ScorerUi(
                        playerId = leader.playerId,
                        playerName = player.name,
                        teamName = team?.name ?: "Sin equipo",
                        totalPoints = leader.totalPoints
                    )
                }

                StatsUiState(topScorers = items)
            }
            .asLiveData()

    class Factory(
        private val playerStatDao: PlayerStatDao,
        private val playerDao: PlayerDao,
        private val teamDao: TeamDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(playerStatDao, playerDao, teamDao) as T
        }
    }
}
