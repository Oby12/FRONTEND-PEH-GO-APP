package com.example.peh_goapp.ui.screen.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.dto.RegisterRequest
import com.example.peh_goapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val role: String = "user",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameError = validateUsername(username)
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = validateName(name)
            )
        }
    }

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

    fun register(onRegisterSuccess: () -> Unit) {
        val username = uiState.value.username
        val name = uiState.value.name
        val email = uiState.value.email
        val password = uiState.value.password

        // Validate inputs
        val usernameError = validateUsername(username)
        val nameError = validateName(name)
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (usernameError != null || nameError != null || emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    usernameError = usernameError,
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        // Proceed with registration
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val registerRequest = RegisterRequest(
                //role = "user",
                username = username,
                name = name,
                email = email,
                password = password
            )

            when (val result = userRepository.register(registerRequest)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    onRegisterSuccess()
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun validateUsername(username: String): String? {
        if (username.isBlank()) {
            return "Username tidak boleh kosong"
        }
        return null
    }

    private fun validateName(name: String): String? {
        if (name.isBlank()) {
            return "Name tidak boleh kosong"
        }
        return null
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