package com.example.hooptracker.ui.tournament

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Tournament
import com.example.hooptracker.databinding.FragmentTournamentDetailBinding
import com.example.hooptracker.domain.UserRole
import com.example.hooptracker.ui.auth.AuthViewModel
import com.example.hooptracker.ui.matchlist.MatchAdapter
import java.text.SimpleDateFormat
import java.util.*
/**
 * Pantalla para ver y editar un torneo, gestionar sus partidos y modificar fechas y descripción.
 */

class TournamentDetailFragment : Fragment(R.layout.fragment_tournament_detail) {

    private var _binding: FragmentTournamentDetailBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    private val viewModel: TournamentDetailViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        val tournamentId = requireArguments().getLong("tournamentId")
        TournamentDetailViewModel.Factory(
            tournamentId,
            app.database.tournamentDao(),
            app.matchRepository
        )
    }

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var startDateMillisSelected: Long? = null
    private var endDateMillisSelected: Long? = null

    private lateinit var matchesAdapter: MatchAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTournamentDetailBinding.bind(view)

        matchesAdapter = MatchAdapter(
            onClick = { match ->
                val bundle = Bundle().apply {
                    putLong("matchId", match.id)
                }

                if (match.status.name == "FINISHED") {
                    findNavController().navigate(
                        R.id.matchSummaryFragment,
                        bundle
                    )
                }
                else {
                    findNavController().navigate(
                        R.id.matchLiveFragment,
                        bundle
                    )
                }
            },

            onLongClick = { match ->

            }
        )

        binding.recyclerTournamentMatches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = matchesAdapter
        }

        viewModel.matches.observe(viewLifecycleOwner) { list ->
            matchesAdapter.submitList(list)
            binding.txtEmptyMatches.isVisible = list.isNullOrEmpty()
        }



        viewModel.tournament.observe(viewLifecycleOwner) { t ->
            if (t != null) {
                binding.edtTournamentName.setText(t.name)

                startDateMillisSelected = t.startDateMillis
                endDateMillisSelected = t.endDateMillis

                binding.edtTournamentStartDate.setText(
                    dateFormatter.format(Date(t.startDateMillis))
                )

                binding.edtTournamentEndDate.setText(
                    t.endDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""
                )

                binding.edtTournamentDescription.setText(t.description ?: "")
            } else {
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar el torneo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // DatePickers
        binding.edtTournamentStartDate.setOnClickListener { showDatePicker(isStart = true) }
        binding.edtTournamentEndDate.setOnClickListener { showDatePicker(isStart = false) }


        authViewModel.user.observe(viewLifecycleOwner) { user ->
            val isAdmin = user?.role == UserRole.TOURNAMENT_ADMIN
            setEditable(isAdmin)
        }


        binding.btnSaveTournament.setOnClickListener {
            val name = binding.edtTournamentName.text.toString().trim()
            val description = binding.edtTournamentDescription.text.toString().trim()
            val tournamentId = requireArguments().getLong("tournamentId")

            if (name.isBlank()) {
                binding.inputTournamentName.error = "El nombre es obligatorio"
                return@setOnClickListener
            } else {
                binding.inputTournamentName.error = null
            }

            val current = viewModel.tournament.value

            val startMillis = startDateMillisSelected
                ?: current?.startDateMillis
                ?: System.currentTimeMillis()

            val endMillis = endDateMillisSelected ?: current?.endDateMillis

            val updated = Tournament(
                id = tournamentId,
                name = name,
                startDateMillis = startMillis,
                endDateMillis = endMillis,
                description = if (description.isBlank()) null else description
            )

            viewModel.updateTournament(updated)
            Toast.makeText(requireContext(), "Torneo actualizado", Toast.LENGTH_SHORT).show()
        }


        binding.btnAddMatchToTournament.setOnClickListener {
            val tournamentId = requireArguments().getLong("tournamentId")

            val bundle = Bundle().apply {
                putLong("tournamentId", tournamentId)
            }

            findNavController().navigate(
                R.id.action_tournamentDetailFragment_to_matchCreateFragment,
                bundle
            )
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val cal = Calendar.getInstance()

        val baseMillis = if (isStart) {
            startDateMillisSelected ?: viewModel.tournament.value?.startDateMillis
        } else {
            endDateMillisSelected ?: viewModel.tournament.value?.endDateMillis
        }

        if (baseMillis != null) {
            cal.timeInMillis = baseMillis
        }

        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val millis = cal.timeInMillis
            val text = dateFormatter.format(cal.time)

            if (isStart) {
                startDateMillisSelected = millis
                binding.edtTournamentStartDate.setText(text)
            } else {
                endDateMillisSelected = millis
                binding.edtTournamentEndDate.setText(text)
            }
        }

        DatePickerDialog(
            requireContext(),
            listener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setEditable(isAdmin: Boolean) {
        binding.inputTournamentName.isEnabled = isAdmin
        binding.inputTournamentStartDate.isEnabled = isAdmin
        binding.inputTournamentEndDate.isEnabled = isAdmin
        binding.inputTournamentDescription.isEnabled = isAdmin
        binding.btnSaveTournament.isVisible = isAdmin
        binding.btnAddMatchToTournament.isVisible = isAdmin
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
