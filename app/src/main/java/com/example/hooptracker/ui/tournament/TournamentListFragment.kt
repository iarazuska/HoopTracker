package com.example.hooptracker.ui.tournament

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Tournament
import com.example.hooptracker.databinding.FragmentTournamentListBinding
import com.example.hooptracker.domain.UserRole
import com.example.hooptracker.ui.auth.AuthViewModel
/**
 * Lista todos los torneos y permite crearlos, editarlos o eliminarlos según el rol.
 */

class TournamentListFragment : Fragment(R.layout.fragment_tournament_list) {

    private var _binding: FragmentTournamentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TournamentListViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        TournamentListViewModel.Factory(app.database.tournamentDao())
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTournamentListBinding.bind(view)

        val adapter = TournamentAdapter(
            onClick = { tournament ->
                val args = bundleOf("tournamentId" to tournament.id)
                findNavController().navigate(
                    R.id.action_tournamentListFragment_to_tournamentDetailFragment,
                    args
                )
            },
            onLongClick = { tournament ->
                handleLongClick(tournament)
            }
        )

        binding.recyclerTournaments.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerTournaments.adapter = adapter

        viewModel.tournaments.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        authViewModel.user.observe(viewLifecycleOwner) { user ->
            val isAdmin = user?.role == UserRole.TOURNAMENT_ADMIN
            binding.btnAddTournament.isVisible = isAdmin
        }

        binding.btnAddTournament.setOnClickListener {
            findNavController().navigate(
                R.id.action_tournamentListFragment_to_tournamentCreateFragment
            )
        }
    }

    private fun handleLongClick(tournament: Tournament) {
        val role = authViewModel.user.value?.role ?: UserRole.GUEST
        val isAdmin = role == UserRole.TOURNAMENT_ADMIN

        if (!isAdmin) return

        AlertDialog.Builder(requireContext())
            .setTitle(tournament.name)
            .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                when (which) {
                    0 -> {
                        val args = bundleOf("tournamentId" to tournament.id)
                        findNavController().navigate(
                            R.id.action_tournamentListFragment_to_tournamentDetailFragment,
                            args
                        )
                    }
                    1 -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Eliminar torneo")
                            .setMessage("¿Seguro que quieres eliminar el torneo \"${tournament.name}\"?")
                            .setPositiveButton("Eliminar") { _, _ ->
                                viewModel.deleteTournament(tournament)
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
