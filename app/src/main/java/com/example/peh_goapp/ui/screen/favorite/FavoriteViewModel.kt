package com.example.peh_goapp.ui.screen.favorite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.model.DestinationModel
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State UI untuk halaman daftar favorit
 */
data class FavoriteUiState(
    val isLoading: Boolean = false,
    val favorites: List<DestinationModel> = emptyList(),
    val searchQuery: String = "",
    val filteredFavorites: List<DestinationModel> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel untuk mengelola halaman daftar favorit
 */
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val TAG = "FavoriteViewModel"

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Memuat daftar favorit dari repository
     */
    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                Log.d(TAG, "Memulai pengambilan data favorit")

                when (val result = favoriteRepository.getFavorites()) {
                    is ApiResult.Success -> {
                        val favorites = result.data
                        Log.d(TAG, "Berhasil memuat ${favorites.size} favorit")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                favorites = favorites,
                                filteredFavorites = favorites,
                                errorMessage = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error loading favorites: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading favorites: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Memperbarui query pencarian dan memfilter favorit berdasarkan query
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isEmpty()) {
                currentState.favorites
            } else {
                currentState.favorites.filter { destination ->
                    destination.name.contains(query, ignoreCase = true) ||
                            destination.address.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredFavorites = filtered
            )
        }
    }

    /**
     * Menghapus pesan error dari state
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}