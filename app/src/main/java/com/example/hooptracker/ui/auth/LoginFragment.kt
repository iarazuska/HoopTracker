package com.example.hooptracker.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.session.UserSession
import com.example.hooptracker.databinding.FragmentLoginBinding
/**
 * Pantalla de inicio de sesión con validación, login normal, invitado y navegación a registro.
 */

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupListeners()
        observeViewModel()
    }


    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val email = binding.edtEmailLogin.text.toString().trim()
            val password = binding.edtPasswordLogin.text.toString().trim()

            setLoading(true)
            viewModel.login(email, password)
        }

        binding.btnGuest.setOnClickListener {
            setLoading(true)
            viewModel.loginAsGuest()
        }

        binding.btnGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }


    private fun validateForm(): Boolean {
        var ok = true

        val email = binding.edtEmailLogin.text?.toString()?.trim().orEmpty()
        val password = binding.edtPasswordLogin.text?.toString()?.trim().orEmpty()

        // Email
        if (email.isBlank()) {
            binding.inputEmailLogin.error = "Introduce tu email"
            ok = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmailLogin.error = "Email no válido"
            ok = false
        } else {
            binding.inputEmailLogin.error = null
        }

        // Password
        if (password.isBlank()) {
            binding.inputPasswordLogin.error = "La contraseña es obligatoria"
            ok = false
        } else {
            binding.inputPasswordLogin.error = null
        }

        return ok
    }


    private fun observeViewModel() {

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {

                UserSession.saveRole(requireContext(), user.role)

                setLoading(false)

                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        viewModel.authError.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                setLoading(false)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setLoading(isLoading: Boolean) {
        binding.progressLogin.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGuest.isEnabled = !isLoading
        binding.btnGoToRegister.isEnabled = !isLoading
        binding.edtEmailLogin.isEnabled = !isLoading
        binding.edtPasswordLogin.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
