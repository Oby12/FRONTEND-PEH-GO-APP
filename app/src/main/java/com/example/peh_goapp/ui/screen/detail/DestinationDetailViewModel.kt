package com.example.peh_goapp.ui.screen.destinationdetail

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.PictureModel
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.example.peh_goapp.data.repository.DestinationRepository
import com.example.peh_goapp.data.repository.FavoriteRepository
import com.example.peh_goapp.data.repository.StatsRepository // Tambahkan import ini
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DestinationDetailUiState(
    val isLoading: Boolean = true,
    val categoryId: Int = 0,
    val destinationId: Int = 0,
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val urlLocation: String = "",
    val coverImage: Bitmap? = null,
    val pictures: List<PictureModel> = emptyList(),
    val pictureImages: Map<Int, Bitmap?> = emptyMap(),
    val isAdmin: Boolean = false,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false
)

@HiltViewModel
class DestinationDetailViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val base64ImageService: Base64ImageService,
    private val tokenPreference: TokenPreference,
    private val statsRepository: StatsRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val TAG = "DestinationDetailVM"

    private val _uiState = MutableStateFlow(DestinationDetailUiState())
    val uiState: StateFlow<DestinationDetailUiState> = _uiState.asStateFlow()

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Melakukan toggle favorit untuk destinasi ${_uiState.value.destinationId}")

                when (val result = favoriteRepository.toggleFavorite(_uiState.value.destinationId)) {
                    is ApiResult.Success -> {
                        // Update state dengan status favorit baru
                        _uiState.update {
                            it.copy(
                                isFavorite = result.data
                            )
                        }

                        Log.d(TAG, "Toggle favorit berhasil, status baru: ${result.data}")
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.errorMessage
                            )
                        }
                        Log.e(TAG, "Toggle favorit gagal: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
                Log.e(TAG, "Exception saat toggle favorit: ${e.message}", e)
            }
        }
    }

    fun loadDestinationDetail(categoryId: Int, destinationId: Int) {
        _uiState.update {
            it.copy(
                isLoading = true,
                categoryId = categoryId,
                destinationId = destinationId,
                isAdmin = tokenPreference.isAdmin()
            )
        }



        viewModelScope.launch {
            try {
                Log.d(TAG, "Memuat detail destinasi: $destinationId")
                when (val result = destinationRepository.getDestinationDetail(categoryId, destinationId)) {
                    is ApiResult.Success -> {
                        val destination = result.data

                        _uiState.update {
                            it.copy(
                                name = destination.name,
                                address = destination.address,
                                description = destination.description,
                                urlLocation = destination.urlLocation,
                                pictures = destination.pictures
                            )
                        }

                        // Load cover image
                        loadCoverImage(destinationId)

                        // Load pictures
                        destination.pictures.forEach { picture ->
                            loadPictureImage(picture.id)
                        }

                        // Catat view destinasi
                        recordDestinationView(destinationId)

                        //periksa status favorite
                        checkFavoriteStatus(destinationId)
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    // Fungsi baru untuk memeriksa status favorit
    private fun checkFavoriteStatus(destinationId: Int) {
        viewModelScope.launch {
            try {
                when (val result = favoriteRepository.checkIsFavorite(destinationId)) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isFavorite = result.data
                            )
                        }
                        Log.d(TAG, "Status favorit untuk destinasi $destinationId: ${result.data}")
                    }
                    is ApiResult.Error -> {
                        // Jangan tampilkan error jika gagal cek favorit
                        Log.e(TAG, "Error checking favorite: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception checking favorite status: ${e.message}", e)
            }
        }
    }


    // Fungsi untuk mencatat view destinasi
    private suspend fun recordDestinationView(destinationId: Int) {
        try {
            Log.d(TAG, "Mencatat view untuk destinasi: $destinationId")

            when (val result = statsRepository.recordDestinationView(destinationId)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Berhasil mencatat view")
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Gagal mencatat view: ${result.errorMessage}")
                    // Tidak perlu menampilkan error ke user jika gagal mencatat view
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception saat mencatat view: ${e.message}", e)
            // Tidak perlu menampilkan error ke user jika gagal mencatat view
        }
    }

    private suspend fun loadCoverImage(destinationId: Int) {
        try {
            Log.d(TAG, "Memuat gambar cover untuk destinasi: $destinationId")
            val bitmap = base64ImageService.getDestinationCoverImage(destinationId)
            _uiState.update {
                it.copy(
                    coverImage = bitmap,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cover image: ${e.message}", e)
            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadPictureImage(pictureId: Int) {
        try {
            Log.d(TAG, "Memuat gambar tambahan: $pictureId")
            val bitmap = base64ImageService.getPictureImage(pictureId)
            _uiState.update {
                val updatedPictureImages = it.pictureImages.toMutableMap()
                updatedPictureImages[pictureId] = bitmap
                it.copy(pictureImages = updatedPictureImages)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading picture image: ${e.message}", e)
        }
    }

    fun deleteDestination(onDeleteSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val categoryId = _uiState.value.categoryId
                val destinationId = _uiState.value.destinationId

                Log.d(TAG, "Menghapus destinasi: $destinationId")
                when (val result = destinationRepository.deleteDestination(categoryId, destinationId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Berhasil menghapus destinasi")
                        _uiState.update { it.copy(isLoading = false) }
                        onDeleteSuccess()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error menghapus: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception menghapus: ${e.message}", e)
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

    fun setErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}