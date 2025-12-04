package com.example.hooptracker.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hooptracker.R
import com.example.hooptracker.data.session.UserSession
/**
 * Pantalla inicial que decide si ir al login o al home según el rol guardado.
 */

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.postDelayed({
            val role = UserSession.getRole(requireContext())

            if (role == null) {
                findNavController().navigate(R.id.action_splash_to_login)
            } else {
                findNavController().navigate(R.id.action_splash_to_homeFragment)
            }
        }, 800)
    }
}
