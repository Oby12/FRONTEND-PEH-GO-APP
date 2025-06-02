package com.example.peh_goapp.ui.screen.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.local.TokenPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enum untuk menentukan tujuan navigasi setelah splash screen
 */
enum class NavigationState {
    LOADING,
    TO_INTRODUCTION,
    TO_LOGIN,
    TO_HOME
}

/**
 * State untuk splash screen
 */
data class SplashUiState(
    val isLoading: Boolean = true,
    val navigationState: NavigationState = NavigationState.LOADING
)

/**
 * ViewModel untuk splash screen yang menentukan tujuan navigasi
 * berdasarkan status login dan apakah user sudah pernah melihat introduction
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenPreference: TokenPreference
) : ViewModel() {

    private val TAG = "SplashViewModel"

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    /**
     * Memeriksa kemana user harus diarahkan setelah splash screen
     *
     * Logika navigasi:
     * 1. Jika first time (belum pernah buka app) -> Introduction
     * 2. Jika sudah pernah buka app tapi belum login -> Login
     * 3. Jika sudah login -> Home
     */
    fun checkNavigationDestination() {
        viewModelScope.launch {
            try {
                // Simulasi loading (opsional, bisa dihapus jika tidak perlu)
                delay(500)

                val isFirstTime = tokenPreference.isFirstTime()
                val hasToken = tokenPreference.getToken().isNotBlank()

                Log.d(TAG, "Checking navigation - isFirstTime: $isFirstTime, hasToken: $hasToken")

                val destinationState = when {
                    isFirstTime -> {
                        Log.d(TAG, "Navigating to Introduction (first time)")
                        NavigationState.TO_INTRODUCTION
                    }
                    hasToken -> {
                        Log.d(TAG, "Navigating to Home (user already logged in)")
                        NavigationState.TO_HOME
                    }
                    else -> {
                        Log.d(TAG, "Navigating to Login (user not logged in)")
                        NavigationState.TO_LOGIN
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        navigationState = destinationState
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error checking navigation destination: ${e.message}", e)

                // Default ke login jika terjadi error
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        navigationState = NavigationState.TO_LOGIN
                    )
                }
            }
        }
    }

    /**
     * Reset state (jika diperlukan)
     */
    fun resetState() {
        _uiState.update {
            SplashUiState()
        }
    }
}