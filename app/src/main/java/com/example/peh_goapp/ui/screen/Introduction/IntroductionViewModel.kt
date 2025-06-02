package com.example.peh_goapp.ui.screen.introduction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.local.TokenPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State untuk introduction screen
 */
data class IntroductionUiState(
    val currentPage: Int = 0,
    val isFinished: Boolean = false
)

/**
 * ViewModel untuk introduction screen
 * Mengelola state introduction dan menyimpan status bahwa user sudah melihat introduction
 */
@HiltViewModel
class IntroductionViewModel @Inject constructor(
    private val tokenPreference: TokenPreference
) : ViewModel() {

    private val TAG = "IntroductionViewModel"

    private val _uiState = MutableStateFlow(IntroductionUiState())
    val uiState: StateFlow<IntroductionUiState> = _uiState.asStateFlow()

    /**
     * Update halaman saat ini
     */
    fun updateCurrentPage(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    /**
     * Menyelesaikan introduction dan menyimpan status bahwa user sudah melihat introduction
     * Setelah ini, introduction tidak akan ditampilkan lagi di masa depan
     */
    fun finishIntroduction() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Finishing introduction - marking as not first time")

                // Simpan status bahwa user sudah melihat introduction
                tokenPreference.setNotFirstTime()

                // Update state untuk trigger navigasi
                _uiState.update {
                    it.copy(isFinished = true)
                }

                Log.d(TAG, "Introduction finished successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error finishing introduction: ${e.message}", e)

                // Tetap finish meskipun ada error
                _uiState.update {
                    it.copy(isFinished = true)
                }
            }
        }
    }

    /**
     * Reset state jika diperlukan
     */
    fun resetState() {
        _uiState.update {
            IntroductionUiState()
        }
    }
}