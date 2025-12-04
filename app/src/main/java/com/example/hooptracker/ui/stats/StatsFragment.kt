package com.example.hooptracker.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.content.res.Configuration
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.databinding.FragmentStatsBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * Pantalla que muestra el top de anotadores usando una gráfica de barras actualizada en tiempo real.
 */

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        StatsViewModel.Factory(
            playerStatDao = app.database.playerStatDao(),
            playerDao = app.database.playerDao(),
            teamDao = app.database.teamDao()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupChart()

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateChart(state)
        }
    }

    private fun isDarkMode(): Boolean {
        val uiMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupChart() {
        val chart = binding.chartTopScorers
        val dark = isDarkMode()

        val textColor = if (dark) Color.WHITE else Color.BLACK
        val gridColor = if (dark) Color.parseColor("#444444") else Color.parseColor("#DDDDDD")
        val axisLineColor = if (dark) Color.LTGRAY else Color.DKGRAY

        chart.description.isEnabled = false
        chart.setFitBars(true)
        chart.setDrawGridBackground(false)
        chart.setBackgroundColor(Color.TRANSPARENT)

        chart.setNoDataText("Todavía no hay estadísticas")
        chart.setNoDataTextColor(textColor)

        val xAxis = chart.xAxis
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = textColor
        xAxis.axisLineColor = axisLineColor

        val leftAxis = chart.axisLeft
        leftAxis.textColor = textColor
        leftAxis.gridColor = gridColor
        leftAxis.axisLineColor = axisLineColor

        chart.axisRight.isEnabled = false

        chart.legend.isEnabled = true
        chart.legend.textColor = textColor
    }

    private fun updateChart(state: StatsUiState) {
        val chart = binding.chartTopScorers

        if (state.topScorers.isEmpty()) {
            chart.clear()
            return
        }

        val entries = state.topScorers.mapIndexed { index, scorer ->
            BarEntry(index.toFloat(), scorer.totalPoints.toFloat())
        }

        val labels = state.topScorers.map { it.playerName }

        val dark = isDarkMode()
        val barColor = if (dark)
            requireContext().getColor(R.color.orange_primary)
        else
            requireContext().getColor(R.color.orange_primary)

        val valueColor = if (dark) Color.WHITE else Color.BLACK

        val dataSet = BarDataSet(entries, "Puntos totales").apply {
            color = barColor
            valueTextColor = valueColor
            valueTextSize = 10f
        }

        val data = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelRotationAngle = -45f

        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
