package com.example.hooptracker.ui.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hooptracker.data.local.dao.TournamentDao
import com.example.hooptracker.data.local.entity.Tournament
import kotlinx.coroutines.launch
/**
 * Proporciona la lista de torneos y permite borrar torneos desde el repositorio.
 */

class TournamentListViewModel(
    private val tournamentDao: TournamentDao
) : ViewModel() {

    val tournaments = tournamentDao.observeTournaments().asLiveData()

    fun deleteTournament(tournament: Tournament) {
        viewModelScope.launch {
            tournamentDao.deleteTournament(tournament)
        }
    }

    class Factory(
        private val tournamentDao: TournamentDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TournamentListViewModel(tournamentDao) as T
        }
    }
}
