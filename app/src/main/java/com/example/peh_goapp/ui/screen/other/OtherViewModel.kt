package com.example.peh_goapp.ui.screen.other

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.OtherModel
import com.example.peh_goapp.data.repository.OtherRepository
import com.example.peh_goapp.data.remote.api.ApiResult // FIX IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State untuk layar Other list
 */
data class OtherUiState(
    val isLoading: Boolean = false,
    val others: List<OtherModel> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class OtherViewModel @Inject constructor(
    private val otherRepository: OtherRepository,
    private val tokenPreference: TokenPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtherUiState())
    val uiState: StateFlow<OtherUiState> = _uiState.asStateFlow()

    fun loadOthers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = otherRepository.getAllOthers()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            others = result.data
                        )
                    }
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

    fun isAdmin(): Boolean {
        return tokenPreference.getRole() == "ADMIN"
    }

    // ADD MISSING METHOD
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}