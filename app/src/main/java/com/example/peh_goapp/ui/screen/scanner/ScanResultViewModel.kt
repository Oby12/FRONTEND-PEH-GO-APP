package com.example.peh_goapp.ui.screen.scanresult

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

/**
 * State untuk layar hasil scan QR code
 */
data class ScanResultUiState(
    val isLoading: Boolean = true,
    val categoryId: Int = 0,
    val destinationId: Int = 0,
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val coverImage: Bitmap? = null,
    val pictureImages: List<Bitmap> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel untuk layar hasil scan QR code
 */
@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val base64ImageService: Base64ImageService
) : ViewModel() {
    private val TAG = "ScanResultViewModel"

    private val _uiState = MutableStateFlow(ScanResultUiState())
    val uiState: StateFlow<ScanResultUiState> = _uiState.asStateFlow()

    /**
     * Memuat detail destinasi berdasarkan ID dari hasil scan
     */
    fun loadDestinationDetail(categoryId: Int, destinationId: Int) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                categoryId = categoryId,
                destinationId = destinationId
            )
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading destination detail: categoryId=$categoryId, destinationId=$destinationId")

                when (val result = destinationRepository.getDestinationDetail(categoryId, destinationId)) {
                    is ApiResult.Success -> {
                        val destination = result.data

                        Log.d(TAG, "Successfully loaded destination: ${destination.name}")

                        _uiState.update {
                            it.copy(
                                name = destination.name,
                                address = destination.address,
                                description = destination.description
                            )
                        }

                        // Load cover image
                        try {
                            val coverImage = base64ImageService.getDestinationCoverImage(destinationId)
                            _uiState.update { it.copy(coverImage = coverImage) }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading cover image: ${e.message}")
                        }

                        // Load picture images
                        val pictureImages = mutableListOf<Bitmap>()
                        destination.pictures.forEach { picture ->
                            try {
                                val bitmap = base64ImageService.getPictureImage(picture.id)
                                bitmap?.let {
                                    pictureImages.add(it)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error loading picture image: ${e.message}")
                            }
                        }

                        _uiState.update {
                            it.copy(
                                pictureImages = pictureImages,
                                isLoading = false
                            )
                        }
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
}