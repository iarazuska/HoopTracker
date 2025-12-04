package com.example.hooptracker.ui.matchlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.MatchStatus
import com.example.hooptracker.databinding.FragmentMatchListBinding
import com.example.hooptracker.domain.UserRole
import com.example.hooptracker.ui.auth.AuthViewModel
/**
 * Pantalla que muestra todos los partidos y permite gestionarlos según el rol del usuario.
 */

class MatchListFragment : Fragment(R.layout.fragment_match_list) {

    private var _binding: FragmentMatchListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchListViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        MatchListViewModel.Factory(app.matchRepository)
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMatchListBinding.bind(view)

        val adapter = MatchAdapter(
            onClick = { match -> openMatch(match) },
            onLongClick = { match -> confirmDelete(match) }
        )

        binding.recyclerMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMatches.adapter = adapter

        viewModel.matches.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.txtEmptyMatches.isVisible = list.isNullOrEmpty()
        }

        authViewModel.user.observe(viewLifecycleOwner) { user ->
            val role = user?.role ?: UserRole.GUEST
            val canManage = role == UserRole.COACH || role == UserRole.TOURNAMENT_ADMIN

            binding.btnCreateMatch.isVisible = canManage
            binding.btnGoToTeams.isVisible = canManage

            binding.txtRoleInfo.text = when (role) {
                UserRole.COACH -> "Modo ENTRENADOR: puedes gestionar equipos y partidos."
                UserRole.TOURNAMENT_ADMIN -> "Modo ADMINISTRADOR: gestión de torneos y partidos."
                else -> "Modo INVITADO: solo visualizar estadísticas."
            }
        }

        binding.btnCreateMatch.setOnClickListener {
            findNavController().navigate(R.id.action_matchListFragment_to_matchCreateFragment)
        }

        binding.btnGoToTeams.setOnClickListener {
            findNavController().navigate(R.id.teamListFragment)
        }

        binding.btnGoToStats.setOnClickListener {
            findNavController().navigate(R.id.statsFragment)
        }
    }

    private fun openMatch(match: Match) {
        val bundle = Bundle().apply { putLong("matchId", match.id) }

        if (match.status == MatchStatus.FINISHED) {
            findNavController().navigate(R.id.matchSummaryFragment, bundle)
        } else {
            findNavController().navigate(R.id.matchLiveFragment, bundle)
        }
    }

    private fun confirmDelete(match: Match) {
        val role = authViewModel.user.value?.role ?: UserRole.GUEST
        val canManage = role == UserRole.COACH || role == UserRole.TOURNAMENT_ADMIN

        if (!canManage) return

        AlertDialog.Builder(requireContext())
            .setTitle("Partido ${match.id}")
            .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                when (which) {
                    0 -> {
                        val bundle = Bundle().apply { putLong("matchId", match.id) }
                        findNavController().navigate(
                            R.id.action_matchListFragment_to_matchCreateFragment,
                            bundle
                        )
                    }
                    1 -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Eliminar partido")
                            .setMessage("¿Seguro que quieres eliminar el partido ${match.id}?")
                            .setPositiveButton("Eliminar") { _, _ ->
                                viewModel.deleteMatch(match)
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
