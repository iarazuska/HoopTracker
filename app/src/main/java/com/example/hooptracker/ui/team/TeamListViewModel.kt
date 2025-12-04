package com.example.hooptracker.ui.team

import androidx.lifecycle.*
import com.example.hooptracker.data.local.entity.Team
import com.example.hooptracker.data.repository.TeamRepository
import kotlinx.coroutines.launch
/**
 * Proporciona la lista de equipos y permite guardar o borrar equipos desde el repositorio.
 */

class TeamListViewModel(
    private val repo: TeamRepository
) : ViewModel() {

    val teams: LiveData<List<Team>> = repo.getAllTeams().asLiveData()

    fun saveTeam(team: Team) {
        viewModelScope.launch {
            repo.saveTeam(team)
        }
    }

    fun deleteTeam(team: Team) {
        viewModelScope.launch {
            repo.deleteTeam(team)
        }
    }


    class Factory(
        private val teamRepository: TeamRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TeamListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TeamListViewModel(teamRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

}
