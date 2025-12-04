package com.example.hooptracker.ui.match

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.MatchStatus
import com.example.hooptracker.databinding.FragmentMatchLiveBinding
import com.example.hooptracker.domain.UserRole
import com.example.hooptracker.ui.auth.AuthViewModel
/**
 * Pantalla del partido en vivo donde se registran estadísticas, marcador, permisos por rol y finalización del partido.
 */

class MatchLiveFragment : Fragment(R.layout.fragment_match_live) {

    private var _binding: FragmentMatchLiveBinding? = null
    private val binding get() = _binding!!

    private val matchId: Long by lazy {
        requireArguments().getLong("matchId")
    }

    private val viewModel: MatchLiveViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        MatchLiveViewModel.Factory(
            matchId = matchId,
            matchRepository = app.matchRepository,
            playerDao = app.database.playerDao(),
            playerStatDao = app.database.playerStatDao(),
            teamDao = app.database.teamDao()
        )
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    private lateinit var homeAdapter: PlayerStatAdapter
    private lateinit var awayAdapter: PlayerStatAdapter

    private var canEditByRole = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMatchLiveBinding.bind(view)

        authViewModel.user.observe(viewLifecycleOwner) { user ->
            val role = user?.role ?: UserRole.GUEST
            canEditByRole = role == UserRole.COACH || role == UserRole.TOURNAMENT_ADMIN

            viewModel.uiState.value?.let { state ->
                applyUiState(state)
            }
        }

        // 🔻 Ya no hay botón de siguiente periodo

        homeAdapter = PlayerStatAdapter(
            isHomeTeam = true,
            onAddPoints = { playerId, isHome, delta -> viewModel.addPoints(playerId, isHome, delta) },
            onAddRebound = { playerId, isHome -> viewModel.addRebound(playerId, isHome) },
            onAddAssist = { playerId, isHome -> viewModel.addAssist(playerId, isHome) },
            onAddSteal = { playerId, isHome -> viewModel.addSteal(playerId, isHome) },
            onAddTurnover = { playerId, isHome -> viewModel.addTurnover(playerId, isHome) },
            onAddFoul = { playerId, isHome -> viewModel.addFoul(playerId, isHome) }
        )

        awayAdapter = PlayerStatAdapter(
            isHomeTeam = false,
            onAddPoints = { playerId, isHome, delta -> viewModel.addPoints(playerId, isHome, delta) },
            onAddRebound = { playerId, isHome -> viewModel.addRebound(playerId, isHome) },
            onAddAssist = { playerId, isHome -> viewModel.addAssist(playerId, isHome) },
            onAddSteal = { playerId, isHome -> viewModel.addSteal(playerId, isHome) },
            onAddTurnover = { playerId, isHome -> viewModel.addTurnover(playerId, isHome) },
            onAddFoul = { playerId, isHome -> viewModel.addFoul(playerId, isHome) }
        )

        binding.recyclerHomePlayers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }

        binding.recyclerAwayPlayers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = awayAdapter
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            applyUiState(state)
        }

        binding.btnFinishMatch.setOnClickListener {
            val state = viewModel.uiState.value
            if (state?.status == MatchStatus.FINISHED) return@setOnClickListener

            viewModel.finishMatch {
                Toast.makeText(requireContext(), "Partido finalizado", Toast.LENGTH_SHORT).show()

                val bundle = bundleOf("matchId" to matchId)
                findNavController().navigate(
                    R.id.action_matchLiveFragment_to_matchSummaryFragment,
                    bundle
                )
            }
        }
    }

    private fun applyUiState(state: MatchLiveUiState) {
        binding.txtHomeScore.text = state.homeScore.toString()
        binding.txtAwayScore.text = state.awayScore.toString()

        binding.txtHomeName.text = state.homeTeamName
        binding.txtAwayName.text = state.awayTeamName

        homeAdapter.submitList(state.homePlayers)
        awayAdapter.submitList(state.awayPlayers)


        val finished = state.status == MatchStatus.FINISHED

        val canEdit = canEditByRole && !finished

        homeAdapter.setButtonsEnabled(canEdit)
        awayAdapter.setButtonsEnabled(canEdit)


        binding.btnFinishMatch.isEnabled = canEditByRole && !finished
        binding.btnFinishMatch.text = if (finished) {
            "Partido finalizado"
        } else {
            "Finalizar partido"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
