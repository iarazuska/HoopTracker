package com.example.hooptracker.ui.player

import com.example.hooptracker.data.local.entity.Player
/**
 * Modelos para representar cabeceras de equipo y filas de jugador en listas agrupadas.
 */

sealed class PlayerListItem {

    data class TeamHeader(
        val teamId: Long,
        val teamName: String,
        var expanded: Boolean = false,
        val players: List<Player>
    ) : PlayerListItem()

    data class PlayerRow(
        val player: Player
    ) : PlayerListItem()
}
