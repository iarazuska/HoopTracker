package com.example.hooptracker.data.repository

import com.example.hooptracker.data.local.dao.MatchDao
import com.example.hooptracker.data.local.entity.Match
import kotlinx.coroutines.flow.Flow
/**
 * Maneja la creación, actualización, eliminación y consulta de partidos.
 */

class MatchRepository(
    private val matchDao: MatchDao
) {

    fun observeMatches(): Flow<List<Match>> = matchDao.observeMatches()

    suspend fun insertMatch(match: Match): Long = matchDao.insert(match)

    suspend fun updateMatch(match: Match) = matchDao.update(match)

    suspend fun deleteMatch(match: Match) = matchDao.delete(match)

    suspend fun getMatchById(id: Long): Match? = matchDao.getMatchById(id)
}
