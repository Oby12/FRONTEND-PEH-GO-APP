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
    val errorMessage: String? = null
)

@HiltViewModel
class DestinationDetailViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val base64ImageService: Base64ImageService,
    private val tokenPreference: TokenPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow(DestinationDetailUiState())
    val uiState: StateFlow<DestinationDetailUiState> = _uiState.asStateFlow()

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
                Log.d("DestinationDetailVM", "Memuat detail destinasi: $destinationId")
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
                    }
                    is ApiResult.Error -> {
                        Log.e("DestinationDetailVM", "Error: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DestinationDetailVM", "Exception: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadCoverImage(destinationId: Int) {
        try {
            Log.d("DestinationDetailVM", "Memuat gambar cover untuk destinasi: $destinationId")
            val bitmap = base64ImageService.getDestinationCoverImage(destinationId)
            _uiState.update {
                it.copy(
                    coverImage = bitmap,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e("DestinationDetailVM", "Error loading cover image: ${e.message}", e)
            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadPictureImage(pictureId: Int) {
        try {
            Log.d("DestinationDetailVM", "Memuat gambar tambahan: $pictureId")
            val bitmap = base64ImageService.getPictureImage(pictureId)
            _uiState.update {
                val updatedPictureImages = it.pictureImages.toMutableMap()
                updatedPictureImages[pictureId] = bitmap
                it.copy(pictureImages = updatedPictureImages)
            }
        } catch (e: Exception) {
            Log.e("DestinationDetailVM", "Error loading picture image: ${e.message}", e)
        }
    }

    fun deleteDestination(onDeleteSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val categoryId = _uiState.value.categoryId
                val destinationId = _uiState.value.destinationId

                Log.d("DestinationDetailVM", "Menghapus destinasi: $destinationId")
                when (val result = destinationRepository.deleteDestination(categoryId, destinationId)) {
                    is ApiResult.Success -> {
                        Log.d("DestinationDetailVM", "Berhasil menghapus destinasi")
                        _uiState.update { it.copy(isLoading = false) }
                        onDeleteSuccess()
                    }
                    is ApiResult.Error -> {
                        Log.e("DestinationDetailVM", "Error menghapus: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DestinationDetailVM", "Exception menghapus: ${e.message}", e)
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