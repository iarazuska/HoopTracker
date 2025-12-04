package com.example.hooptracker.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.HoopTrackerApp
import com.example.hooptracker.R
import com.example.hooptracker.data.session.UserSession
import com.example.hooptracker.databinding.FragmentRegisterBinding
import com.example.hooptracker.domain.UserRole
/**
 * Pantalla de registro que valida datos, permite elegir rol y crea el usuario.
 */

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as HoopTrackerApp
        AuthViewModel.Factory(app.authRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupRolesSpinner()
        setupListeners()
        observeViewModel()
    }

    private fun setupRolesSpinner() {
        val rolesUi = listOf("Entrenador", "Administrador torneo")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            rolesUi
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnRole.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()


            val role = when (binding.spnRole.selectedItemPosition) {
                0 -> UserRole.COACH
                1 -> UserRole.TOURNAMENT_ADMIN
                else -> UserRole.GUEST
            }

            binding.btnRegister.isEnabled = false
            viewModel.register(name, email, password, role)
        }

        binding.btnGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }


    private fun validateForm(): Boolean {
        var ok = true

        val name = binding.edtName.text?.toString()?.trim().orEmpty()
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        val password = binding.edtPassword.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            binding.inputName.error = "Introduce tu nombre"
            ok = false
        } else binding.inputName.error = null

        if (email.isBlank()) {
            binding.inputEmail.error = "El email es obligatorio"
            ok = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.error = "Formato de email no válido"
            ok = false
        } else binding.inputEmail.error = null

        if (password.isBlank()) {
            binding.inputPassword.error = "La contraseña es obligatoria"
            ok = false
        } else if (password.length < 6) {
            binding.inputPassword.error = "Mínimo 6 caracteres"
            ok = false
        } else binding.inputPassword.error = null

        return ok
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                UserSession.saveRole(requireContext(), user.role)
                findNavController().navigate(R.id.action_register_to_homeFragment)
            }
        }

        viewModel.authError.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                binding.btnRegister.isEnabled = true
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
