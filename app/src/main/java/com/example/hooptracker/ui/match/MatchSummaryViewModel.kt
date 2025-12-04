package com.example.hooptracker.ui.match

import androidx.lifecycle.*
import com.example.hooptracker.data.local.dao.PlayerDao
import com.example.hooptracker.data.local.dao.PlayerStatDao
import com.example.hooptracker.data.local.dao.TeamDao
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.PlayerStat
import com.example.hooptracker.data.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
/**
 * Carga los datos finales del partido y genera el resumen: equipos, estadísticas y marcador final.
 */

data class MatchSummaryUiState(
    val match: Match? = null,
    val homeTeamName: String = "Local",
    val awayTeamName: String = "Visitante",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homePlayers: List<PlayerWithStats> = emptyList(),
    val awayPlayers: List<PlayerWithStats> = emptyList()
)

class MatchSummaryViewModel(
    private val matchId: Long,
    private val matchRepository: MatchRepository,
    private val playerDao: PlayerDao,
    private val playerStatDao: PlayerStatDao,
    private val teamDao: TeamDao
) : ViewModel() {

    private val _uiState = MutableLiveData(MatchSummaryUiState())
    val uiState: LiveData<MatchSummaryUiState> = _uiState

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch(Dispatchers.IO) {
            val match = matchRepository.getMatchById(matchId) ?: return@launch

            val homeTeam = teamDao.getTeamById(match.homeTeamId)
            val awayTeam = teamDao.getTeamById(match.awayTeamId)

            val homePlayers = playerDao.getPlayersByTeam(match.homeTeamId)
            val awayPlayers = playerDao.getPlayersByTeam(match.awayTeamId)

            val allStats = mutableListOf<PlayerStat>()

            for (p in homePlayers) {
                val s = playerStatDao.getStatForPlayer(matchId, p.id)
                if (s != null) allStats.add(s)
            }

            for (p in awayPlayers) {
                val s = playerStatDao.getStatForPlayer(matchId, p.id)
                if (s != null) allStats.add(s)
            }

            val homeList = homePlayers.mapNotNull { player ->
                val stat = allStats.firstOrNull { s -> s.playerId == player.id } ?: return@mapNotNull null
                PlayerWithStats(player = player, stats = stat)
            }

            val awayList = awayPlayers.mapNotNull { player ->
                val stat = allStats.firstOrNull { s -> s.playerId == player.id } ?: return@mapNotNull null
                PlayerWithStats(player = player, stats = stat)
            }

            val homeScore = homeList.sumOf { it.stats.points }
            val awayScore = awayList.sumOf { it.stats.points }

            _uiState.postValue(
                MatchSummaryUiState(
                    match = match,
                    homeTeamName = homeTeam?.name ?: "Local",
                    awayTeamName = awayTeam?.name ?: "Visitante",
                    homeScore = homeScore,
                    awayScore = awayScore,
                    homePlayers = homeList,
                    awayPlayers = awayList
                )
            )
        }
    }

    class Factory(
        private val matchId: Long,
        private val matchRepository: MatchRepository,
        private val playerDao: PlayerDao,
        private val playerStatDao: PlayerStatDao,
        private val teamDao: TeamDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MatchSummaryViewModel(
                matchId,
                matchRepository,
                playerDao,
                playerStatDao,
                teamDao
            ) as T
        }
    }
}
