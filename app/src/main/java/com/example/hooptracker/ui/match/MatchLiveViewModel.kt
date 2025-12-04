package com.example.hooptracker.ui.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hooptracker.data.local.dao.PlayerDao
import com.example.hooptracker.data.local.dao.PlayerStatDao
import com.example.hooptracker.data.local.dao.TeamDao
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.MatchStatus
import com.example.hooptracker.data.local.entity.Player
import com.example.hooptracker.data.local.entity.PlayerStat
import com.example.hooptracker.data.local.entity.Team
import com.example.hooptracker.data.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * Gestiona el estado del partido en vivo: jugadores, estadísticas, marcador y actualización en tiempo real.
 */

data class PlayerWithStats(
    val player: Player,
    val stats: PlayerStat
)

data class MatchLiveUiState(
    val match: Match? = null,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homePlayers: List<PlayerWithStats> = emptyList(),
    val awayPlayers: List<PlayerWithStats> = emptyList(),
    val status: MatchStatus = MatchStatus.NOT_STARTED,
    val period: Int = 1,
    val homeTeamName: String = "Local",
    val awayTeamName: String = "Visitante"
)

class MatchLiveViewModel(
    private val matchId: Long,
    private val matchRepository: MatchRepository,
    private val playerDao: PlayerDao,
    private val playerStatDao: PlayerStatDao,
    private val teamDao: TeamDao
) : ViewModel() {

    private val _uiState = MutableLiveData(MatchLiveUiState())
    val uiState: LiveData<MatchLiveUiState> = _uiState

    init {
        loadPlayersAndStats()
    }

    private fun loadPlayersAndStats() {
        viewModelScope.launch(Dispatchers.IO) {

            val match = matchRepository.getMatchById(matchId) ?: return@launch

            val homeTeam: Team? = teamDao.getTeamById(match.homeTeamId)
            val awayTeam: Team? = teamDao.getTeamById(match.awayTeamId)

            val homeTeamPlayers = playerDao.getPlayersByTeam(match.homeTeamId)
            val awayTeamPlayers = playerDao.getPlayersByTeam(match.awayTeamId)

            val allStats = mutableListOf<PlayerStat>()

            for (p in homeTeamPlayers) {
                val s = playerStatDao.getStatForPlayer(matchId, p.id)
                    ?: createEmptyStats(p.id, isHome = true)
                allStats.add(s)
            }

            for (p in awayTeamPlayers) {
                val s = playerStatDao.getStatForPlayer(matchId, p.id)
                    ?: createEmptyStats(p.id, isHome = false)
                allStats.add(s)
            }

            val homeList = homeTeamPlayers.map { p ->
                PlayerWithStats(
                    player = p,
                    stats = allStats.first { it.playerId == p.id }
                )
            }

            val awayList = awayTeamPlayers.map { p ->
                PlayerWithStats(
                    player = p,
                    stats = allStats.first { it.playerId == p.id }
                )
            }

            val homeScore = homeList.sumOf { it.stats.points }
            val awayScore = awayList.sumOf { it.stats.points }

            _uiState.postValue(
                MatchLiveUiState(
                    match = match,
                    homePlayers = homeList,
                    awayPlayers = awayList,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    status = match.status,
                    period = match.period,
                    homeTeamName = homeTeam?.name ?: "Local",
                    awayTeamName = awayTeam?.name ?: "Visitante"
                )
            )
        }
    }

    private suspend fun createEmptyStats(playerId: Long, isHome: Boolean): PlayerStat {
        val stat = PlayerStat(
            matchId = matchId,
            playerId = playerId,
            isHomeTeam = isHome
        )
        playerStatDao.insert(stat)
        return stat
    }

    fun addPoints(playerId: Long, isHome: Boolean, delta: Int) {
        updateStat(playerId) { stat ->
            stat.copy(points = stat.points + delta)
        }
    }

    fun addRebound(playerId: Long, isHome: Boolean) {
        updateStat(playerId) { stat ->
            stat.copy(rebounds = stat.rebounds + 1)
        }
    }

    fun addAssist(playerId: Long, isHome: Boolean) {
        updateStat(playerId) { stat ->
            stat.copy(assists = stat.assists + 1)
        }
    }

    fun addSteal(playerId: Long, isHome: Boolean) {
        updateStat(playerId) { stat ->
            stat.copy(steals = stat.steals + 1)
        }
    }

    fun addTurnover(playerId: Long, isHome: Boolean) {
        updateStat(playerId) { stat ->
            stat.copy(turnovers = stat.turnovers + 1)
        }
    }

    fun addFoul(playerId: Long, isHome: Boolean) {
        updateStat(playerId) { stat ->
            stat.copy(fouls = (stat.fouls + 1).coerceAtMost(5))
        }
    }

    private fun updateStat(playerId: Long, update: (PlayerStat) -> PlayerStat) {
        viewModelScope.launch(Dispatchers.IO) {

            val stat = playerStatDao.getStatForPlayer(matchId, playerId)
                ?: return@launch

            val updated = update(stat)
            playerStatDao.insert(updated)

            loadPlayersAndStats()
        }
    }

    fun startMatchIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            val match = matchRepository.getMatchById(matchId) ?: return@launch
            if (match.status == MatchStatus.NOT_STARTED) {
                val updated = match.copy(status = MatchStatus.IN_PROGRESS)
                matchRepository.updateMatch(updated)
                loadPlayersAndStats()
            }
        }
    }

    fun nextPeriod() {
        viewModelScope.launch(Dispatchers.IO) {
            val match = matchRepository.getMatchById(matchId) ?: return@launch
            val newPeriod = (match.period + 1).coerceAtMost(4)
            if (newPeriod != match.period) {
                val updated = match.copy(period = newPeriod)
                matchRepository.updateMatch(updated)
                loadPlayersAndStats()
            }
        }
    }

    fun finishMatch(onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {

            val match = matchRepository.getMatchById(matchId) ?: return@launch

            val stats = playerStatDao.getByMatch(matchId)

            val homePoints = stats.filter { it.isHomeTeam }.sumOf { it.points }
            val awayPoints = stats.filter { !it.isHomeTeam }.sumOf { it.points }

            val updated = match.copy(
                status = MatchStatus.FINISHED,
                homeScore = homePoints,
                awayScore = awayPoints
            )
            matchRepository.updateMatch(updated)

            loadPlayersAndStats()

            withContext(Dispatchers.Main) {
                onFinished()
            }
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
            return MatchLiveViewModel(
                matchId,
                matchRepository,
                playerDao,
                playerStatDao,
                teamDao
            ) as T
        }
    }
}
