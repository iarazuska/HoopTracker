package com.example.hooptracker.ui.match

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Team
import com.example.hooptracker.databinding.FragmentMatchCreateBinding
/**
 * Pantalla para crear un partido eligiendo equipos, fecha y torneo, con validación y navegación al partido en vivo.
 */

class MatchCreateFragment : Fragment(R.layout.fragment_match_create) {

    private var _binding: FragmentMatchCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchCreateViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        MatchCreateViewModel.Factory(
            app.matchRepository,
            app.teamRepository,
            app.database.tournamentDao()
        )
    }

    private var teams: List<Team> = emptyList()

    private var selectedHomeId: Long? = null
    private var selectedAwayId: Long? = null

    private val dateFormatter =
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    private var selectedDateMillis: Long? = null

    private val tournamentIdFromArgs: Long? by lazy {
        val arg = arguments?.getLong("tournamentId", -1L) ?: -1L
        if (arg > 0) arg else null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMatchCreateBinding.bind(view)

        if (tournamentIdFromArgs != null) {
            binding.inputTournament.isVisible = false
        }

        binding.editMatchDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker() }
        }

        observeTeams()
        setupListeners()
    }

    private fun showDatePicker() {
        val cal = java.util.Calendar.getInstance()

        val listener = android.app.DatePickerDialog.OnDateSetListener { _, year, month, day ->
            cal.set(year, month, day, 0, 0, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)

            selectedDateMillis = cal.timeInMillis
            binding.editMatchDate.setText(dateFormatter.format(cal.time))
            binding.inputMatchDate.error = null
        }

        android.app.DatePickerDialog(
            requireContext(),
            listener,
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun observeTeams() {
        viewModel.teams.observe(viewLifecycleOwner) { list ->
            teams = list

            if (teams.size < 2) {
                Toast.makeText(
                    requireContext(),
                    "Debes tener al menos 2 equipos",
                    Toast.LENGTH_SHORT
                ).show()
                return@observe
            }

            val names = teams.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)

            binding.dropHomeTeam.setAdapter(adapter)
            binding.dropAwayTeam.setAdapter(adapter)

            binding.dropHomeTeam.setOnItemClickListener { _, _, position, _ ->
                selectedHomeId = teams[position].id
                binding.inputHomeTeam.error = null
            }

            binding.dropAwayTeam.setOnItemClickListener { _, _, position, _ ->
                selectedAwayId = teams[position].id
                binding.inputAwayTeam.error = null
            }
        }
    }

    private fun setupListeners() {
        binding.btnCreateMatch.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val homeId = selectedHomeId!!
            val awayId = selectedAwayId!!

            val dateMillis = selectedDateMillis
            if (dateMillis == null) {
                binding.inputMatchDate.error = "Selecciona una fecha"
                return@setOnClickListener
            }

            val tournamentName: String? =
                if (tournamentIdFromArgs != null) {
                    null
                } else {
                    binding.editTournamentName.text.toString().trim().ifBlank { null }
                }

            setLoading(true)

            viewModel.createMatch(
                homeId = homeId,
                awayId = awayId,
                dateMillis = dateMillis,
                tournamentName = tournamentName,
                tournamentId = tournamentIdFromArgs
            ) { matchId ->
                val bundle = bundleOf("matchId" to matchId)
                findNavController().navigate(R.id.matchLiveFragment, bundle)
            }
        }
    }

    private fun validate(): Boolean {
        var ok = true

        if (selectedHomeId == null) {
            binding.inputHomeTeam.error = "Elige equipo local"
            ok = false
        } else {
            binding.inputHomeTeam.error = null
        }

        if (selectedAwayId == null) {
            binding.inputAwayTeam.error = "Elige equipo visitante"
            ok = false
        } else {
            binding.inputAwayTeam.error = null
        }

        if (selectedDateMillis == null) {
            binding.inputMatchDate.error = "La fecha es obligatoria"
            ok = false
        } else {
            binding.inputMatchDate.error = null
        }

        return ok
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCreateMatch.isEnabled = !loading
        binding.progressMatchCreate.isVisible = loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
