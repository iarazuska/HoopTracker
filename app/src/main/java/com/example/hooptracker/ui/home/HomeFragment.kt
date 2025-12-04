package com.example.hooptracker.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.databinding.FragmentHomeBinding
import com.example.hooptracker.ui.auth.AuthViewModel
/**
 * Pantalla principal con accesos a las secciones, logout y cambio de tema claro/oscuro.
 */

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        setupButtons()
        setupThemeToggle()
    }

    private fun setupButtons() {
        binding.btnHomeMatches.setOnClickListener {
            findNavController().navigate(R.id.matchListFragment)
        }
        binding.btnHomeTeams.setOnClickListener {
            findNavController().navigate(R.id.teamListFragment)
        }
        binding.btnHomePlayers.setOnClickListener {
            findNavController().navigate(R.id.playerListFragment)
        }
        binding.btnHomeTournaments.setOnClickListener {
            findNavController().navigate(R.id.tournamentListFragment)
        }
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun setupThemeToggle() {
        val prefs =
            requireActivity().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val darkMode = prefs.getBoolean("dark_mode_enabled", false)

        binding.btnThemeToggle.setImageResource(
            if (darkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
        )

        binding.btnThemeToggle.setOnClickListener {
            val currentDark = prefs.getBoolean("dark_mode_enabled", false)
            val newDark = !currentDark

            prefs.edit().putBoolean("dark_mode_enabled", newDark).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (newDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            binding.btnThemeToggle.setImageResource(
                if (newDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
