package com.example.hooptracker.ui.player

import android.os.Bundle
import android.view.View
import android.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Player
import com.example.hooptracker.databinding.FragmentPlayerListBinding
import com.example.hooptracker.domain.UserRole
import com.example.hooptracker.ui.auth.AuthViewModel
/**
 * Lista jugadores globales o por equipo, con acciones de ver, editar, eliminar y agrupación por equipos.
 */

class PlayerListFragment : Fragment(R.layout.fragment_player_list) {

    private var _binding: FragmentPlayerListBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    private val viewModel: PlayerListViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        PlayerListViewModel.Factory(app.playerRepository)
    }

    private var teamId: Long = 0L
    private var teamName: String? = null

    private lateinit var adapter: PlayerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerListBinding.bind(view)

        teamId = requireArguments().getLong("teamId", 0L)
        teamName = requireArguments().getString("teamName")

        val isGlobal = teamId == 0L

        binding.txtPlayersTitle.text =
            if (isGlobal) "Jugadores" else "Jugadores - ${teamName ?: ""}"

        adapter = PlayerAdapter(
            onClick = { player ->
                if (player.position == "__HEADER__") return@PlayerAdapter
                val bundle = bundleOf(
                    "teamId" to player.teamId,
                    "playerId" to player.id,
                    "mode" to "view"
                )
                findNavController().navigate(R.id.playerDetailFragment, bundle)
            },
            onLongClick = { player ->
                if (player.position == "__HEADER__") return@PlayerAdapter
                handleLongClick(player)
            }
        )

        binding.recyclerPlayers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPlayers.adapter = adapter

        binding.progressPlayers.isVisible = true
        binding.txtEmptyPlayers.isVisible = false
        binding.recyclerPlayers.isVisible = false

        viewModel.setTeamId(teamId)

        viewModel.players.observe(viewLifecycleOwner) { list ->
            binding.progressPlayers.isVisible = false

            if (list.isNullOrEmpty()) {
                binding.recyclerPlayers.isVisible = false
                binding.txtEmptyPlayers.isVisible = true
            } else {
                binding.recyclerPlayers.isVisible = true
                binding.txtEmptyPlayers.isVisible = false

                if (isGlobal) adapter.submitGroupedList(list)
                else adapter.submitFlatList(list)
            }
        }

        authViewModel.user.observe(viewLifecycleOwner) { user ->
            val canEdit = user?.role == UserRole.COACH || user?.role == UserRole.TOURNAMENT_ADMIN
            binding.fabAddPlayer.isVisible = canEdit && !isGlobal
        }

        binding.fabAddPlayer.setOnClickListener {
            val bundle = bundleOf(
                "teamId" to teamId,
                "playerId" to 0L,
                "mode" to "edit"
            )
            findNavController().navigate(R.id.playerDetailFragment, bundle)
        }
    }

    private fun handleLongClick(player: Player) {
        val user = authViewModel.user.value
        val canEdit = user?.role == UserRole.COACH || user?.role == UserRole.TOURNAMENT_ADMIN
        if (!canEdit) return

        AlertDialog.Builder(requireContext())
            .setTitle(player.name)
            .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                when (which) {
                    0 -> {
                        val bundle = bundleOf(
                            "teamId" to player.teamId,
                            "playerId" to player.id,
                            "mode" to "edit"
                        )
                        findNavController().navigate(R.id.playerDetailFragment, bundle)
                    }
                    1 -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Eliminar jugador")
                            .setMessage("¿Seguro que quieres eliminar a ${player.name}?")
                            .setPositiveButton("Eliminar") { _, _ ->
                                viewModel.deletePlayer(player)
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
