package com.example.hooptracker.ui.match

import androidx.lifecycle.*
import com.example.hooptracker.data.local.dao.TournamentDao
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.MatchStatus
import com.example.hooptracker.data.local.entity.Tournament
import com.example.hooptracker.data.repository.MatchRepository
import com.example.hooptracker.data.repository.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * Lógica para crear partidos y torneos nuevos, guardarlos y devolver el ID creado.
 */

class MatchCreateViewModel(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
    private val tournamentDao: TournamentDao
) : ViewModel() {

    val teams = teamRepository.observeTeams().asLiveData()

    fun createMatch(
        homeId: Long,
        awayId: Long,
        dateMillis: Long,
        tournamentName: String?,
        tournamentId: Long?,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            var finalTournamentId = tournamentId

            if (finalTournamentId == null && !tournamentName.isNullOrBlank()) {
                val createdTournament = Tournament(
                    name = tournamentName,
                    startDateMillis = dateMillis,
                    endDateMillis = null,
                    description = null
                )
                finalTournamentId = tournamentDao.insert(createdTournament)
            }

            val match = Match(
                homeTeamId = homeId,
                awayTeamId = awayId,
                dateMillis = dateMillis,
                status = MatchStatus.NOT_STARTED,
                period = 1,
                tournamentId = finalTournamentId
            )

            val id = matchRepository.insertMatch(match)

            withContext(Dispatchers.Main) {
                onCreated(id)
            }
        }
    }

    class Factory(
        private val matchRepository: MatchRepository,
        private val teamRepository: TeamRepository,
        private val tournamentDao: TournamentDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MatchCreateViewModel(
                matchRepository,
                teamRepository,
                tournamentDao
            ) as T
        }
    }
}
