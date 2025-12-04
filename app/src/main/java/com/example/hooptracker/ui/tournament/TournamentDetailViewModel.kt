package com.example.hooptracker.ui.tournament

import androidx.lifecycle.*
import com.example.hooptracker.data.local.dao.TournamentDao
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.Tournament
import com.example.hooptracker.data.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
/**
 * Carga y actualiza la información del torneo y obtiene sus partidos asociados.
 */

class TournamentDetailViewModel(
    private val tournamentId: Long,
    private val tournamentDao: TournamentDao,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _tournament = MutableLiveData<Tournament?>()
    val tournament: LiveData<Tournament?> = _tournament

    val matches: LiveData<List<Match>> =
        matchRepository.observeMatches()
            .map { list -> list.filter { it.tournamentId == tournamentId } }
            .asLiveData()

    init {
        loadTournament()
    }

    private fun loadTournament() {
        viewModelScope.launch(Dispatchers.IO) {
            val t = tournamentDao.getById(tournamentId)
            _tournament.postValue(t)
        }
    }

    fun updateTournament(tournament: Tournament) {
        viewModelScope.launch(Dispatchers.IO) {
            tournamentDao.update(tournament)
            _tournament.postValue(tournament)
        }
    }

    class Factory(
        private val tournamentId: Long,
        private val tournamentDao: TournamentDao,
        private val matchRepository: MatchRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TournamentDetailViewModel(
                tournamentId,
                tournamentDao,
                matchRepository
            ) as T
        }
    }
}
