package com.example.hooptracker.data.repository

import com.example.hooptracker.data.local.dao.TeamDao
import com.example.hooptracker.data.local.entity.Team
import kotlinx.coroutines.flow.Flow
/**
 * Gestiona equipos: creación, eliminación y observación en tiempo real.
 */

class TeamRepository(
    private val teamDao: TeamDao
) {

    fun observeTeams(): Flow<List<Team>> = teamDao.observeTeams()

    fun getAllTeams(): Flow<List<Team>> = teamDao.observeTeams()

    suspend fun getTeamById(id: Long): Team? = teamDao.getTeamById(id)

    suspend fun saveTeam(team: Team): Long = teamDao.insert(team)

    suspend fun saveTeams(teams: List<Team>) = teamDao.insertAll(teams)

    suspend fun deleteTeam(team: Team) = teamDao.delete(team)
}
