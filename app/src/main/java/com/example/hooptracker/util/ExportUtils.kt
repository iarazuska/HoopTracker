package com.example.hooptracker.util

import android.content.Context
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.ui.match.MatchSummaryUiState
import com.example.hooptracker.ui.player.PlayerHistoryUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
/**
 * Utilidades para generar y exportar archivos CSV y XML de partidos e historial de jugadores.
 */

object ExportUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private fun formatDate(millis: Long?): String {
        if (millis == null || millis <= 0L) return ""
        return dateFormat.format(Date(millis))
    }



    fun buildMatchCsv(state: MatchSummaryUiState): String {
        val match = state.match
        val dateStr = formatDate(match?.dateMillis)

        val sb = StringBuilder()

        sb.appendLine("Partido;${state.homeTeamName} vs ${state.awayTeamName}")
        sb.appendLine("Fecha;$dateStr")
        sb.appendLine("Marcador;${state.homeScore}-${state.awayScore}")
        sb.appendLine()
        sb.appendLine("Equipo;Jugador;PTS;REB;AST;ROB;PER")

        state.homePlayers.forEach { p ->
            sb.appendLine(
                "${state.homeTeamName};${p.player.name};" +
                        "${p.stats.points};${p.stats.rebounds};" +
                        "${p.stats.assists};${p.stats.steals};${p.stats.turnovers}"
            )
        }

        state.awayPlayers.forEach { p ->
            sb.appendLine(
                "${state.awayTeamName};${p.player.name};" +
                        "${p.stats.points};${p.stats.rebounds};" +
                        "${p.stats.assists};${p.stats.steals};${p.stats.turnovers}"
            )
        }

        return sb.toString()
    }

    fun buildMatchXml(state: MatchSummaryUiState): String {
        val match = state.match
        val dateStr = formatDate(match?.dateMillis)

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("<matchSummary>")
        sb.appendLine("  <homeTeam>${state.homeTeamName}</homeTeam>")
        sb.appendLine("  <awayTeam>${state.awayTeamName}</awayTeam>")
        sb.appendLine("  <date>$dateStr</date>")
        sb.appendLine("  <score>${state.homeScore}-${state.awayScore}</score>")

        sb.appendLine("  <homePlayers>")
        state.homePlayers.forEach { p ->
            sb.appendLine("    <player>")
            sb.appendLine("      <name>${p.player.name}</name>")
            sb.appendLine("      <points>${p.stats.points}</points>")
            sb.appendLine("      <rebounds>${p.stats.rebounds}</rebounds>")
            sb.appendLine("      <assists>${p.stats.assists}</assists>")
            sb.appendLine("      <steals>${p.stats.steals}</steals>")
            sb.appendLine("      <turnovers>${p.stats.turnovers}</turnovers>")
            sb.appendLine("    </player>")
        }
        sb.appendLine("  </homePlayers>")

        sb.appendLine("  <awayPlayers>")
        state.awayPlayers.forEach { p ->
            sb.appendLine("    <player>")
            sb.appendLine("      <name>${p.player.name}</name>")
            sb.appendLine("      <points>${p.stats.points}</points>")
            sb.appendLine("      <rebounds>${p.stats.rebounds}</rebounds>")
            sb.appendLine("      <assists>${p.stats.assists}</assists>")
            sb.appendLine("      <steals>${p.stats.steals}</steals>")
            sb.appendLine("      <turnovers>${p.stats.turnovers}</turnovers>")
            sb.appendLine("    </player>")
        }
        sb.appendLine("  </awayPlayers>")

        sb.appendLine("</matchSummary>")

        return sb.toString()
    }

    fun exportMatchToCsv(
        context: Context,
        matchId: Long,
        state: MatchSummaryUiState
    ): File {
        val content = buildMatchCsv(state)
        val fileName = "match_${matchId}.csv"
        return saveTextToCache(context, fileName, content)
    }


    fun exportMatchToXml(
        context: Context,
        matchId: Long,
        state: MatchSummaryUiState
    ): File {
        val content = buildMatchXml(state)
        val fileName = "match_${matchId}.xml"
        return saveTextToCache(context, fileName, content)
    }

    private fun saveTextToCache(
        context: Context,
        fileName: String,
        content: String
    ): File {
        val dir = context.cacheDir
        val file = File(dir, fileName)
        file.writeText(content, Charsets.UTF_8)
        return file
    }


    fun buildPlayerHistoryCsv(state: PlayerHistoryUiState): String {
        val sb = StringBuilder()

        sb.appendLine("Jugador;${state.playerName}")
        sb.appendLine("Media de puntos;${"%.2f".format(state.avgPoints)}")
        sb.appendLine()
        sb.appendLine("Partido;Fecha;Puntos")

        state.matches.forEachIndexed { index, match: Match ->
            val dateStr = formatDate(match.dateMillis)
            val pts = state.pointsPerMatch.getOrNull(index) ?: 0
            val title = "${match.homeTeamId} vs ${match.awayTeamId}"
            sb.appendLine("$title;$dateStr;$pts")
        }

        return sb.toString()
    }

    fun buildPlayerHistoryXml(state: PlayerHistoryUiState): String {
        val sb = StringBuilder()

        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("<playerHistory>")
        sb.appendLine("  <playerName>${state.playerName}</playerName>")
        sb.appendLine("  <averagePoints>${"%.2f".format(state.avgPoints)}</averagePoints>")
        sb.appendLine("  <matches>")

        state.matches.forEachIndexed { index, match: Match ->
            val dateStr = formatDate(match.dateMillis)
            val pts = state.pointsPerMatch.getOrNull(index) ?: 0
            val title = "${match.homeTeamId} vs ${match.awayTeamId}"

            sb.appendLine("    <match>")
            sb.appendLine("      <title>$title</title>")
            sb.appendLine("      <date>$dateStr</date>")
            sb.appendLine("      <points>$pts</points>")
            sb.appendLine("    </match>")
        }

        sb.appendLine("  </matches>")
        sb.appendLine("</playerHistory>")

        return sb.toString()
    }
}
