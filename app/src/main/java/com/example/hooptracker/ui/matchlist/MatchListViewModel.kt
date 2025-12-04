package com.example.hooptracker.ui.matchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
/**
 * Proporciona la lista de partidos y permite eliminar uno desde el repositorio.
 */

class MatchListViewModel(
    private val repository: MatchRepository
) : ViewModel() {

    val matches: LiveData<List<Match>> = repository.observeMatches().asLiveData()

    fun deleteMatch(match: Match) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMatch(match)
        }
    }

    class Factory(
        private val repository: MatchRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MatchListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MatchListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
