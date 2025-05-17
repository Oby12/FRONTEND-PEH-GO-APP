package com.example.peh_goapp.ui.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.R
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.dto.LoginRequest
import com.example.peh_goapp.data.repository.UserRepository
import com.example.peh_goapp.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state untuk halaman login
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk halaman login yang mengikuti best practice
 * - Menggunakan Hilt untuk dependency injection
 * - Menggunakan ResourceProvider untuk akses resource string
 * - Menggunakan flow untuk state management
 * - Memisahkan validasi dari logika bisnis
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenPreference: TokenPreference,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private val TAG = "LoginViewModel"

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Update email dan validasi format
     */
    fun updateEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
    }

    /**
     * Update password dan validasi
     */
    fun updatePassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = validatePassword(password)
            )
        }
    }

    /**
     * Mengatasi proses login
     */
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
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val loginRequest = LoginRequest(
                email = email,
                password = password
            )

            // Sebelum login, extract username dari email untuk digunakan jika nama tidak ada dari backend
            val usernameFromEmail = email.substringBefore('@')

            Log.d(TAG, "Attempting login with email: $email")

            when (val result = userRepository.login(loginRequest)) {
                is ApiResult.Success -> {
                    // Tambahan: simpan nama pengguna dari email jika belum tersimpan
                    if (tokenPreference.getName().isBlank()) {
                        tokenPreference.saveName(usernameFromEmail)
                        Log.d(TAG, "Saved name from email as fallback: $usernameFromEmail")
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    onLoginSuccess()
                }
                is ApiResult.Error -> {
                    // Gunakan ResourceProvider untuk mendapatkan error message yang sudah diterjemahkan
                    val localizedError = resourceProvider.getErrorMessage(result.errorMessage)

                    Log.e(TAG, "Login error: ${result.errorMessage}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = localizedError
                        )
                    }
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Validasi format email
     */
    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return resourceProvider.getString(R.string.error_email_empty)
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        if (!email.matches(emailRegex.toRegex())) {
            return resourceProvider.getString(R.string.error_email_invalid)
        }

        return null
    }

    /**
     * Validasi password
     */
    private fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return resourceProvider.getString(R.string.error_password_empty)
        }

        if (password.length < 6) {
            return resourceProvider.getString(R.string.error_password_too_short)
        }

        return null
    }
}