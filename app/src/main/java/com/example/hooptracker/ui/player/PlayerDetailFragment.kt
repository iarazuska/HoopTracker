package com.example.hooptracker.ui.player

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Player
import com.example.hooptracker.databinding.FragmentPlayerDetailBinding
import com.example.hooptracker.domain.UserRole
import com.example.hooptracker.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * Pantalla para ver o editar un jugador, mostrando estadísticas, validación y permisos según el rol.
 */

class PlayerDetailFragment : Fragment(R.layout.fragment_player_detail) {

    private var _binding: FragmentPlayerDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerListViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        PlayerListViewModel.Factory(app.playerRepository)
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    private var teamId: Long = 0L
    private var playerId: Long = 0L
    private var mode: String = "view"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerDetailBinding.bind(view)

        teamId = requireArguments().getLong("teamId")
        playerId = requireArguments().getLong("playerId", 0L)
        mode = requireArguments().getString("mode", if (playerId == 0L) "edit" else "view")

        if (teamId == 0L) {
            Toast.makeText(requireContext(), "Error: el jugador debe pertenecer a un equipo", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val app = requireActivity().application as HoopTrackerApp
        val db = app.database
        val playerStatDao = db.playerStatDao()
        val playerDao = db.playerDao()

        if (playerId != 0L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val player = playerDao.getPlayerById(playerId)

                withContext(Dispatchers.Main) {
                    if (player != null) {
                        binding.edtPlayerName.setText(player.name)
                        binding.edtPlayerNumber.setText(player.number.toString())
                        binding.edtPlayerPosition.setText(player.position)
                    }
                }
            }
        }

        if (mode == "view") {
            lifecycleScope.launch(Dispatchers.IO) {
                val stats = playerStatDao.getStatsForPlayer(playerId)

                val totalPoints = stats.sumOf { it.points }
                val totalRebounds = stats.sumOf { it.rebounds }
                val totalAssists = stats.sumOf { it.assists }
                val totalSteals = stats.sumOf { it.steals }
                val totalTurnovers = stats.sumOf { it.turnovers }

                withContext(Dispatchers.Main) {
                    if (stats.isEmpty()) {
                        binding.txtPlayerTotals.text = "Sin estadísticas registradas todavía"
                    } else {
                        val games = stats.size.toDouble()

                        val avgPoints = totalPoints.toDouble() / games
                        val avgRebounds = totalRebounds.toDouble() / games
                        val avgAssists = totalAssists.toDouble() / games
                        val avgSteals = totalSteals.toDouble() / games
                        val avgTurnovers = totalTurnovers.toDouble() / games

                        binding.txtPlayerTotals.text =
                            "Estadísticas totales:\n" +
                                    "PTS: $totalPoints   REB: $totalRebounds   AST: $totalAssists\n" +
                                    "ROB: $totalSteals   PER: $totalTurnovers\n\n" +
                                    "Media por partido:\n" +
                                    "PTS: ${"%.1f".format(avgPoints)}   " +
                                    "REB: ${"%.1f".format(avgRebounds)}   " +
                                    "AST: ${"%.1f".format(avgAssists)}\n" +
                                    "ROB: ${"%.1f".format(avgSteals)}   " +
                                    "PER: ${"%.1f".format(avgTurnovers)}"
                    }
                }
            }
        }

        applyModeUi()
        setupListeners()

        authViewModel.user.observe(viewLifecycleOwner) { user ->
            val canEdit = user?.role == UserRole.COACH || user?.role == UserRole.TOURNAMENT_ADMIN

            if (mode == "edit" && !canEdit) {
                Toast.makeText(requireContext(), "No tienes permisos para editar jugadores", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun applyModeUi() {
        if (mode == "view") {
            binding.inputPlayerName.isVisible = true
            binding.inputPlayerNumber.isVisible = true
            binding.inputPlayerPosition.isVisible = true

            binding.edtPlayerName.isEnabled = false
            binding.edtPlayerNumber.isEnabled = false
            binding.edtPlayerPosition.isEnabled = false

            binding.btnSavePlayer.isVisible = false
            binding.btnDeletePlayer.isVisible = false
            binding.progressPlayer.isVisible = false
            binding.txtPlayerTotals.isVisible = true
        } else {
            binding.inputPlayerName.isVisible = true
            binding.inputPlayerNumber.isVisible = true
            binding.inputPlayerPosition.isVisible = true

            binding.edtPlayerName.isEnabled = true
            binding.edtPlayerNumber.isEnabled = true
            binding.edtPlayerPosition.isEnabled = true

            binding.btnSavePlayer.isVisible = true
            binding.txtPlayerTotals.isVisible = false
            binding.progressPlayer.isVisible = false
            binding.btnDeletePlayer.isVisible = false
        }
    }

    private fun setupListeners() {
        binding.btnSavePlayer.setOnClickListener {
            if (mode != "edit") return@setOnClickListener
            if (!validate()) return@setOnClickListener

            if (teamId == 0L) {
                Toast.makeText(requireContext(), "Error: jugador sin equipo, no se puede guardar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = binding.edtPlayerName.text.toString().trim()
            val numberText = binding.edtPlayerNumber.text.toString().trim()
            val number = numberText.toIntOrNull()

            if (number == null) {
                binding.inputPlayerNumber.error = "Debe ser un número"
                return@setOnClickListener
            }

            val position = binding.edtPlayerPosition.text.toString().trim()

            val player = Player(
                id = playerId,
                teamId = teamId,
                name = name,
                number = number,
                position = position
            )

            setLoading(true)
            viewModel.savePlayer(player)
            Toast.makeText(requireContext(), "Jugador guardado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.btnDeletePlayer.setOnClickListener {}
    }

    private fun validate(): Boolean {
        var ok = true

        val name = binding.edtPlayerName.text?.toString()?.trim().orEmpty()
        val number = binding.edtPlayerNumber.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            binding.inputPlayerName.error = "El nombre es obligatorio"
            ok = false
        } else binding.inputPlayerName.error = null

        if (number.isBlank()) {
            binding.inputPlayerNumber.error = "Dorsal obligatorio"
            ok = false
        } else if (number.toIntOrNull() == null) {
            binding.inputPlayerNumber.error = "Debe ser un número"
            ok = false
        } else binding.inputPlayerNumber.error = null

        return ok
    }

    private fun setLoading(load: Boolean) {
        binding.progressPlayer.isVisible = load
        binding.btnSavePlayer.isEnabled = !load
        binding.inputPlayerName.isEnabled = !load
        binding.inputPlayerNumber.isEnabled = !load
        binding.inputPlayerPosition.isEnabled = !load
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
