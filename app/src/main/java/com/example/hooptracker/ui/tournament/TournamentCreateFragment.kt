package com.example.hooptracker.ui.tournament

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Tournament
import com.example.hooptracker.databinding.FragmentTournamentCreateBinding
import java.text.SimpleDateFormat
import java.util.*
/**
 * Pantalla para crear un torneo con nombre, fechas y descripción usando date pickers.
 */

class TournamentCreateFragment : Fragment(R.layout.fragment_tournament_create) {

    private var _binding: FragmentTournamentCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TournamentCreateViewModel by viewModels {
        val app = requireActivity().application as HoopTrackerApp
        TournamentCreateViewModel.Factory(app.database.tournamentDao())
    }

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTournamentCreateBinding.bind(view)

        binding.edtTournamentStartDate.setOnClickListener {
            showDatePicker(isStart = true)
        }

        binding.edtTournamentEndDate.setOnClickListener {
            showDatePicker(isStart = false)
        }

        binding.btnCreateTournament.setOnClickListener {
            saveTournament()
        }
    }

    private fun saveTournament() {
        val name = binding.edtTournamentName.text.toString().trim()
        val description = binding.edtTournamentDescription.text.toString().trim()

        if (name.isBlank()) {
            binding.inputTournamentName.error = "El nombre es obligatorio"
            return
        }

        val start = startDateMillis ?: System.currentTimeMillis()

        val tournament = Tournament(
            id = 0,
            name = name,
            startDateMillis = start,
            endDateMillis = endDateMillis,
            description = if (description.isBlank()) null else description
        )

        viewModel.insertTournament(tournament)

        Toast.makeText(requireContext(), "Torneo creado correctamente", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun showDatePicker(isStart: Boolean) {
        val cal = Calendar.getInstance()

        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            cal.set(year, month, day)
            val millis = cal.timeInMillis
            val text = dateFormatter.format(cal.time)

            if (isStart) {
                startDateMillis = millis
                binding.edtTournamentStartDate.setText(text)
            } else {
                endDateMillis = millis
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
