package com.example.hooptracker.ui.player

import androidx.lifecycle.*
import com.example.hooptracker.data.local.entity.Player
import com.example.hooptracker.data.repository.PlayerRepository
import kotlinx.coroutines.launch
/**
 * Proporciona la lista de jugadores filtrada por equipo y permite guardar o borrar jugadores.
 */

class PlayerListViewModel(
    private val repo: PlayerRepository
) : ViewModel() {

    private val _teamId = MutableLiveData<Long>(0L)

    val players: LiveData<List<Player>> = _teamId.switchMap { id ->
        if (id == 0L) {
            repo.getAllPlayers().asLiveData()
        } else {
            repo.getPlayersByTeam(id).asLiveData()
        }
    }

    fun setTeamId(teamId: Long) {
        _teamId.value = teamId
    }

    fun savePlayer(player: Player) {
        viewModelScope.launch {
            repo.savePlayer(player)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repo.deletePlayer(player)
        }
    }

    class Factory(
        private val repo: PlayerRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerListViewModel(repo) as T
        }
    }
}
