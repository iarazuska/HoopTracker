package com.example.hooptracker.ui.player

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.databinding.FragmentPlayerHistoryBinding
import com.example.hooptracker.util.ExportUtils
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
/**
 * Muestra el historial del jugador con totales, medias, gráfica de puntos y exportación CSV/XML.
 */

class PlayerHistoryFragment : Fragment(R.layout.fragment_player_history) {

    private var _binding: FragmentPlayerHistoryBinding? = null
    private val binding get() = _binding!!

    private val playerId: Long by lazy {
        requireArguments().getLong("playerId")
    }

    private val viewModel: PlayerHistoryViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        PlayerHistoryViewModel.Factory(
            playerId = playerId,
            playerDao = app.database.playerDao(),
            playerStatDao = app.database.playerStatDao(),
            matchRepository = app.matchRepository
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerHistoryBinding.bind(view)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.txtPlayerNameHistory.text = state.playerName
            binding.txtAveragePoints.text = "Media de puntos: ${"%.1f".format(state.avgPoints)}"

            binding.txtTotals.text =
                "Totales acumulados:\n" +
                        "PTS: ${state.totalPoints}   " +
                        "REB: ${state.totalRebounds}   " +
                        "AST: ${state.totalAssists}\n" +
                        "ROB: ${state.totalSteals}   " +
                        "PER: ${state.totalTurnovers}"

            setupLineChart(state.pointsPerMatch)
        }

        binding.btnExportPlayerHistory.setOnClickListener {
            showExportDialog()
        }
    }

    private fun setupLineChart(points: List<Int>) {
        if (points.isEmpty()) {
            binding.chartPlayerHistory.clear()
            return
        }

        val entries = ArrayList<Entry>()
        points.forEachIndexed { index, value ->
            entries.add(Entry((index + 1).toFloat(), value.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Puntos por partido")
        val data = LineData(dataSet)

        binding.chartPlayerHistory.data = data
        binding.chartPlayerHistory.description.isEnabled = false
        binding.chartPlayerHistory.axisRight.isEnabled = false
        binding.chartPlayerHistory.invalidate()
    }

    private fun showExportDialog() {
        val options = arrayOf("CSV", "XML")
        AlertDialog.Builder(requireContext())
            .setTitle("Exportar historial del jugador")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportHistory("csv")
                    1 -> exportHistory("xml")
                }
            }
            .show()
    }

    private fun exportHistory(format: String) {
        val state = viewModel.uiState.value ?: return

        val content = when (format) {
            "csv" -> ExportUtils.buildPlayerHistoryCsv(state)
            "xml" -> ExportUtils.buildPlayerHistoryXml(state)
            else -> return
        }

        val extension = if (format == "csv") "csv" else "xml"
        val safeName = if (state.playerName.isBlank()) "player" else state.playerName.replace(" ", "_")
        val fileName = "history_${safeName}.$extension"

        val file = saveTextToCache(fileName, content)
        val mime = if (format == "csv") "text/csv" else "text/xml"
        shareFile(file, mime)
    }

    private fun saveTextToCache(fileName: String, content: String): File {
        val dir = requireContext().cacheDir
        val file = File(dir, fileName)
        file.writeText(content, Charsets.UTF_8)
        return file
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
