package com.example.peh_goapp.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.dto.LoginRequest
import com.example.peh_goapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = validatePassword(password)
            )
        }
    }

    fun login(onLoginSuccess: () -> Unit) {
        val email = uiState.value.email
        val password = uiState.value.password

        // Validate inputs
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        // Proceed with login
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val loginRequest = LoginRequest(
                //role = "user",  // Sesuai dengan API yang dijelaskan di user.md
                email = email,
                password = password
            )

            when (val result = userRepository.login(loginRequest)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    onLoginSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.errorMessage
                        )
                    }
                }
            }
        }
    }

    fun loginWithGoogle(onLoginSuccess: () -> Unit) {
        // Implementasi login dengan Google akan ditambahkan nanti
        // Ini biasanya memerlukan Firebase Auth atau sistem autentikasi lainnya

        // Untuk sekarang, tampilkan pesan bahwa fitur sedang dalam pengembangan
        _uiState.update {
            it.copy(
                errorMessage = "Login dengan Google sedang dalam pengembangan."
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "Email tidak boleh kosong"
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        if (!email.matches(emailRegex.toRegex())) {
            return "Format email tidak valid"
        }

        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "Password tidak boleh kosong"
        }

        if (password.length < 6) {
            return "Password minimal 6 karakter"
        }

        return null
    }
}