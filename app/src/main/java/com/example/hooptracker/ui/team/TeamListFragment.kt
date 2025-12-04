package com.example.hooptracker.ui.team

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Team
import com.example.hooptracker.data.local.entity.User
import com.example.hooptracker.databinding.FragmentTeamListBinding
import com.example.hooptracker.domain.UserRole
import com.example.hooptracker.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
/**
 * Lista de equipos con acciones de ver jugadores, crear, editar y eliminar según el rol del usuario.
 */

class TeamListFragment : Fragment(R.layout.fragment_team_list) {

    private var _binding: FragmentTeamListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TeamListViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        TeamListViewModel.Factory(app.teamRepository)
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTeamListBinding.bind(view)

        // ADAPTER con click + long click
        val adapter = TeamAdapter(
            onClick = { team ->
                // Ir a la lista de jugadores de ese equipo
                val bundle = bundleOf(
                    "teamId" to team.id,
                    "teamName" to team.name
                )
                findNavController().navigate(R.id.playerListFragment, bundle)
            },
            onLongClick = { team ->
                handleLongClick(team)
            }
        )

        binding.recyclerTeams.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTeams.adapter = adapter

        // OBSERVAR EQUIPOS
        viewModel.teams.observe(viewLifecycleOwner) { list ->

            binding.progressTeams.isVisible = false

            when {
                list == null -> {
                    binding.recyclerTeams.isVisible = false
                    binding.txtEmptyTeams.isVisible = false
                }
                list.isEmpty() -> {
                    binding.recyclerTeams.isVisible = false
                    binding.txtEmptyTeams.isVisible = true
                }
                else -> {
                    binding.recyclerTeams.isVisible = true
                    binding.txtEmptyTeams.isVisible = false
                    adapter.submitList(list)
                }
            }
        }

        // Spinner inicial
        binding.progressTeams.isVisible = true

        // PERMISOS PARA FAB – SOLO COACH / ADMIN TORNEO
        authViewModel.user.observe(viewLifecycleOwner) { user: User? ->
            val canEdit = user?.role == UserRole.COACH || user?.role == UserRole.TOURNAMENT_ADMIN
            binding.fabAddTeam.isVisible = canEdit
        }

        // AÑADIR EQUIPO
        binding.fabAddTeam.setOnClickListener {
            val bundle = bundleOf("teamId" to 0L)
            findNavController().navigate(R.id.teamDetailFragment, bundle)
        }
    }

    private fun handleLongClick(team: Team) {
        val user = authViewModel.user.value
        val canEdit = user?.role == UserRole.COACH || user?.role == UserRole.TOURNAMENT_ADMIN

        if (!canEdit) return  // Invitado no puede editar / borrar

        AlertDialog.Builder(requireContext())
            .setTitle(team.name)
            .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                when (which) {
                    0 -> {
                        // EDITAR → ir al detalle del equipo
                        val bundle = bundleOf("teamId" to team.id)
                        findNavController().navigate(R.id.teamDetailFragment, bundle)
                    }
                    1 -> {
                        // ELIMINAR con comprobaciones como en TeamDetailFragment
                        AlertDialog.Builder(requireContext())
                            .setTitle("Eliminar equipo")
                            .setMessage("¿Seguro que quieres eliminar el equipo \"${team.name}\"?")
                            .setPositiveButton("Eliminar") { _, _ ->
                                val app = requireActivity().application as HoopTrackerApp
                                val db = app.database

                                lifecycleScope.launch {
                                    // 1️⃣ comprobar si tiene partidos
                                    val matchesCount = db.matchDao().countMatchesForTeam(team.id)

                                    if (matchesCount > 0) {
                                        Toast.makeText(
                                            requireContext(),
                                            "No puedes borrar un equipo que tiene partidos asociados.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@launch
                                    }

                                    // 2️⃣ borrar jugadores del equipo
                                    db.playerDao().deleteByTeamId(team.id)

                                    // 3️⃣ borrar el equipo
                                    db.teamDao().delete(team)

                                    Toast.makeText(
                                        requireContext(),
                                        "Equipo eliminado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
