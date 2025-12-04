package com.example.hooptracker.ui.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hooptracker.data.local.dao.TournamentDao
import com.example.hooptracker.data.local.entity.Tournament
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
/**
 * Inserta un nuevo torneo en la base de datos desde el formulario de creación.
 */

class TournamentCreateViewModel(
    private val dao: TournamentDao
) : ViewModel() {

    fun insertTournament(tournament: Tournament) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(tournament)
        }
    }

    class Factory(private val dao: TournamentDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TournamentCreateViewModel(dao) as T
        }
    }
}
