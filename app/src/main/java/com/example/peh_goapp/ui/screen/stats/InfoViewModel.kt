package com.example.peh_goapp.ui.screen.info

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.model.StatsModel
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InfoUiState(
    val isLoading: Boolean = false,
    val stats: StatsModel? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {
    private val TAG = "InfoViewModel"

    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                Log.d(TAG, "Memulai pengambilan data statistik")
                when (val result = statsRepository.getStats()) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Berhasil mendapatkan statistik: ${result.data}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                stats = result.data,
                                errorMessage = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error mendapatkan statistik: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat mengambil statistik: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}