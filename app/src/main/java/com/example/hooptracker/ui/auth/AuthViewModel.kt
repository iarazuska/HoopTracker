package com.example.hooptracker.ui.auth

import androidx.lifecycle.*
import com.example.hooptracker.data.repository.AuthRepository
import com.example.hooptracker.data.local.entity.User
import com.example.hooptracker.domain.UserRole
import kotlinx.coroutines.launch
/**
 * Gestiona el login, registro, sesión e invitado conectándose con AuthRepository.
 */

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _authError = MutableLiveData<String?>()
    val authError: LiveData<String?> = _authError

    init {
        viewModelScope.launch {
            val current = authRepository.getCurrentUser()
            _user.postValue(current)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            if (result != null) {
                _user.postValue(result)
                _authError.postValue(null)
            } else {
                _authError.postValue("Usuario o contraseña incorrectos")
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            val guest = authRepository.loginAsGuest()
            _user.postValue(guest)
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            val result = authRepository.register(name, email, password, role)
            if (result != null) {
                _user.postValue(result)
                _authError.postValue(null)
            } else {
                _authError.postValue("No se pudo registrar el usuario")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _user.postValue(null)
        }
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
