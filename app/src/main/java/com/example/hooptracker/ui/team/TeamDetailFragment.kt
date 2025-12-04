package com.example.hooptracker.ui.team

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Team
import com.example.hooptracker.databinding.FragmentTeamDetailBinding
import com.example.hooptracker.domain.UserRole
import androidx.fragment.app.activityViewModels
import com.example.hooptracker.ui.auth.AuthViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
/**
 * Pantalla para crear o editar un equipo, con validación y control de borrado seguro.
 */

class TeamDetailFragment : Fragment(R.layout.fragment_team_detail) {

    private var _binding: FragmentTeamDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TeamListViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        TeamListViewModel.Factory(app.teamRepository)
    }

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }


    private var teamId: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTeamDetailBinding.bind(view)

        teamId = requireArguments().getLong("teamId", 0L)

        setupListeners()
    }

    private fun setupListeners() {

        binding.btnSaveTeam.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val name = binding.edtTeamName.text.toString().trim()

            val team = Team(
                id = teamId,
                name = name
            )

            setLoading(true)
            viewModel.saveTeam(team)
            Toast.makeText(requireContext(), "Equipo guardado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.btnDeleteTeam.setOnClickListener {

            if (teamId == 0L) return@setOnClickListener

            val app = requireActivity().application as HoopTrackerApp
            val db = app.database

            setLoading(true)

            lifecycleScope.launch {

                // 1️⃣ comprobar si tiene partidos
                val matchesCount = db.matchDao().countMatchesForTeam(teamId)

                if (matchesCount > 0) {
                    setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "No puedes borrar un equipo que tiene partidos asociados.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                // 2️⃣ borrar jugadores del equipo
                db.playerDao().deleteByTeamId(teamId)

                // 3️⃣ borrar el equipo
                val name = binding.edtTeamName.text.toString().trim()
                val team = Team(id = teamId, name = name)

                db.teamDao().delete(team)

                setLoading(false)
                Toast.makeText(requireContext(), "Equipo eliminado correctamente", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }


    }

    private fun validate(): Boolean {
        val name = binding.edtTeamName.text?.toString()?.trim().orEmpty()

        return if (name.isBlank()) {
            binding.inputTeamName.error = "El nombre del equipo es obligatorio"
            false
        } else {
            binding.inputTeamName.error = null
            true
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSaveTeam.isEnabled = !loading
        binding.inputTeamName.isEnabled = !loading
        binding.progressTeam.isVisible = loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
