package com.example.hooptracker.ui.match

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.databinding.FragmentMatchSummaryBinding
import com.example.hooptracker.util.ExportUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.File
/**
 * Pantalla de resumen del partido con totales, rankings, filtros, gráficos y exportación CSV/XML.
 */

class MatchSummaryFragment : Fragment(R.layout.fragment_match_summary) {

    private var _binding: FragmentMatchSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchSummaryViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        val matchId = requireArguments().getLong("matchId")
        MatchSummaryViewModel.Factory(
            matchId = matchId,
            matchRepository = app.matchRepository,
            playerDao = app.database.playerDao(),
            playerStatDao = app.database.playerStatDao(),
            teamDao = app.database.teamDao()
        )
    }

    private lateinit var homeAdapter: PlayerSummaryAdapter
    private lateinit var awayAdapter: PlayerSummaryAdapter

    private var allPlayers: List<PlayerWithStats> = emptyList()
    private var currentPlayerFilter: String = "Todos"
    private var currentStatFilter: String = "Ambos"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMatchSummaryBinding.bind(view)

        homeAdapter = PlayerSummaryAdapter()
        awayAdapter = PlayerSummaryAdapter()

        binding.recyclerHomeSummary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }

        binding.recyclerAwaySummary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = awayAdapter
        }

        val statOptions = listOf("Ambos", "Puntos", "Puntos / Rebotes / Asistencias")
        val statAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statOptions
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerStatType.adapter = statAdapter

        binding.spinnerStatType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentStatFilter = statOptions[position]
                refreshCharts()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerPlayerFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentPlayerFilter = parent.getItemAtPosition(position) as String
                    refreshCharts()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            val homeName = state.homeTeamName
            val awayName = state.awayTeamName

            binding.txtFinalScore.text =
                "Marcador final:\n$homeName ${state.homeScore} - ${state.awayScore} $awayName"

            binding.txtTeamTotalsHome.text =
                "Totales $homeName:\n${buildTeamTotals(state.homePlayers)}"
            binding.txtTeamTotalsAway.text =
                "Totales $awayName:\n${buildTeamTotals(state.awayPlayers)}"

            val playersForLeaders = state.homePlayers + state.awayPlayers
            binding.txtTopScorers.text =
                buildTopListTitle("Mejores anotadores", playersForLeaders) { it.points }

            binding.txtTopRebounders.text =
                buildTopListTitle("Mejores reboteadores", playersForLeaders) { it.rebounds }

            binding.txtTopAssists.text =
                buildTopListTitle("Mejores asistentes", playersForLeaders) { it.assists }

            homeAdapter.submitList(state.homePlayers)
            awayAdapter.submitList(state.awayPlayers)

            allPlayers = playersForLeaders

            val playerNames = mutableListOf("Todos")
            playerNames.addAll(allPlayers.map { it.player.name })

            val playerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                playerNames
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerPlayerFilter.adapter = playerAdapter

            refreshCharts()
        }

        binding.btnExportCsv.setOnClickListener {
            exportMatch("csv")
        }

        binding.btnExportXml.setOnClickListener {
            exportMatch("xml")
        }
    }

    private fun buildTeamTotals(players: List<PlayerWithStats>): String {
        val totalPoints = players.sumOf { it.stats.points }
        val totalReb = players.sumOf { it.stats.rebounds }
        val totalAst = players.sumOf { it.stats.assists }
        val totalStl = players.sumOf { it.stats.steals }
        val totalTov = players.sumOf { it.stats.turnovers }

        return "PTS $totalPoints   REB $totalReb   AST $totalAst   ROB $totalStl   PER $totalTov"
    }

    private fun buildTopListTitle(
        title: String,
        players: List<PlayerWithStats>,
        selector: (stats: com.example.hooptracker.data.local.entity.PlayerStat) -> Int
    ): String {
        val body = players
            .sortedByDescending { selector(it.stats) }
            .take(3)
            .mapIndexed { index, pws ->
                val value = selector(pws.stats)
                "${index + 1}) ${pws.player.name} - $value"
            }
            .joinToString("\n")

        return "$title:\n$body"
    }

    private fun isNightMode(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun chartTextColor(): Int =
        if (isNightMode()) Color.WHITE else Color.BLACK

    private fun chartGridColor(): Int =
        if (isNightMode()) Color.DKGRAY else Color.LTGRAY

    private fun refreshCharts() {
        if (allPlayers.isEmpty()) {
            binding.chartPoints.clear()
            binding.chartPra.clear()
            return
        }

        val filteredPlayers =
            if (currentPlayerFilter == "Todos") {
                allPlayers
            } else {
                allPlayers.filter { it.player.name == currentPlayerFilter }
            }

        if (filteredPlayers.isEmpty()) {
            binding.chartPoints.clear()
            binding.chartPra.clear()
            return
        }

        when (currentStatFilter) {
            "Puntos" -> {
                binding.chartPoints.visibility = View.VISIBLE
                binding.chartPra.visibility = View.GONE
                setupPointsChart(filteredPlayers)
            }

            "Puntos / Rebotes / Asistencias" -> {
                binding.chartPoints.visibility = View.GONE
                binding.chartPra.visibility = View.VISIBLE
                setupPraChart(filteredPlayers)
            }

            else -> {
                binding.chartPoints.visibility = View.VISIBLE
                binding.chartPra.visibility = View.VISIBLE
                setupPointsChart(filteredPlayers)
                setupPraChart(filteredPlayers)
            }
        }
    }

    private fun styleChartBase(chart: com.github.mikephil.charting.charts.BarChart) {
        val textColor = chartTextColor()
        val gridColor = chartGridColor()

        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.setNoDataText("Sin datos")
        chart.setNoDataTextColor(textColor)
        chart.axisRight.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = textColor
        xAxis.axisLineColor = gridColor

        val leftAxis = chart.axisLeft
        leftAxis.textColor = textColor
        leftAxis.gridColor = gridColor
        leftAxis.axisLineColor = gridColor

        chart.legend.isEnabled = true
        chart.legend.textColor = textColor
    }

    private fun setupPointsChart(players: List<PlayerWithStats>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        players.forEachIndexed { index, pws ->
            entries.add(BarEntry(index.toFloat(), pws.stats.points.toFloat()))
            labels.add(pws.player.name)
        }

        val dataSet = BarDataSet(entries, "Puntos por jugador").apply {
            color = requireContext().getColor(R.color.orange_primary)
            valueTextColor = chartTextColor()
            valueTextSize = 10f
        }

        val data = BarData(dataSet)

        binding.chartPoints.data = data
        styleChartBase(binding.chartPoints)

        val xAxis: XAxis = binding.chartPoints.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelRotationAngle = -35f

        binding.chartPoints.invalidate()
    }

    private fun setupPraChart(players: List<PlayerWithStats>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        players.forEachIndexed { index, pws ->
            val s = pws.stats
            entries.add(
                BarEntry(
                    index.toFloat(),
                    floatArrayOf(
                        s.points.toFloat(),
                        s.rebounds.toFloat(),
                        s.assists.toFloat()
                    )
                )
            )
            labels.add(pws.player.name)
        }

        val colorPoints = requireContext().getColor(R.color.orange_primary)
        val colorReb = requireContext().getColor(R.color.orange_dark)
        val colorAst = requireContext().getColor(R.color.orange_light)

        val dataSet = BarDataSet(entries, "P/R/A por jugador").apply {
            stackLabels = arrayOf("Puntos", "Rebotes", "Asistencias")
            setColors(colorPoints, colorReb, colorAst)
            valueTextColor = chartTextColor()
            valueTextSize = 10f
        }

        val data = BarData(dataSet)
        binding.chartPra.data = data

        styleChartBase(binding.chartPra)

        val xAxis: XAxis = binding.chartPra.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelRotationAngle = -35f

        binding.chartPra.invalidate()
    }

    private fun exportMatch(format: String) {
        val state = viewModel.uiState.value
        val match = state?.match
        if (state == null || match == null) {
            return
        }

        val file = when (format) {
            "csv" -> ExportUtils.exportMatchToCsv(
                context = requireContext(),
                matchId = match.id,
                state = state
            )

            "xml" -> ExportUtils.exportMatchToXml(
                context = requireContext(),
                matchId = match.id,
                state = state
            )

            else -> return
        }

        val mimeType = if (format == "csv") "text/csv" else "text/xml"
        shareFile(file, mimeType)
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(sendIntent, "Compartir archivo"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
