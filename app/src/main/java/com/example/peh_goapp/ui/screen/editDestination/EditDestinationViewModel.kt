package com.example.peh_goapp.ui.screen.editdestination

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.model.PictureModel
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.example.peh_goapp.data.repository.DestinationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditDestinationUiState(
    val isLoading: Boolean = true,
    val categoryId: Int = 0,
    val destinationId: Int = 0,
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val urlLocation: String = "",
    val youtubeUrl: String = "", // Field baru
    val coverImage: Bitmap? = null,
    val coverImageUri: Uri? = null,
    val isNewCoverSelected: Boolean = false,
    val pictures: List<PictureModel> = emptyList(),
    val pictureImages: Map<Int, Bitmap?> = emptyMap(),
    val pictureImageUris: List<Uri> = emptyList(),
    val removedPictureIds: List<Int> = emptyList(),
    val nameError: String? = null,
    val addressError: String? = null,
    val descriptionError: String? = null,
    val urlLocationError: String? = null,
    val youtubeUrlError: String? = null, // Field baru
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditDestinationViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val base64ImageService: Base64ImageService
) : ViewModel() {

    private val TAG = "EditDestinationVM"
    private val _uiState = MutableStateFlow(EditDestinationUiState())
    val uiState: StateFlow<EditDestinationUiState> = _uiState.asStateFlow()

    fun loadDestinationDetail(categoryId: Int, destinationId: Int) {
        _uiState.update {
            it.copy(
                isLoading = true,
                categoryId = categoryId,
                destinationId = destinationId,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                when (val result = destinationRepository.getDestinationDetail(categoryId, destinationId)) {
                    is ApiResult.Success -> {
                        val destination = result.data

                        _uiState.update {
                            it.copy(
                                name = destination.name,
                                address = destination.address,
                                description = destination.description,
                                urlLocation = destination.urlLocation,
                                youtubeUrl = destination.youtubeUrl ?: "", // Load YouTube URL
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
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    // Fungsi untuk update YouTube URL
    fun updateYoutubeUrl(url: String) {
        _uiState.update {
            it.copy(
                youtubeUrl = url,
                youtubeUrlError = validateYoutubeUrl(url)
            )
        }
    }

    private fun validateYoutubeUrl(url: String): String? {
        return when {
            url.isNotEmpty() && !isValidYoutubeUrl(url) ->
                "Format URL YouTube tidak valid. Gunakan: youtube.com/watch?v=... atau youtu.be/..."
            else -> null
        }
    }

    private fun isValidYoutubeUrl(url: String): Boolean {
        val patterns = listOf(
            "^(https?://)?(www\\.)?(youtube\\.com/(watch\\?v=|embed/)|youtu\\.be/)\\S+$",
            "^[\\w-]{11}$" // Just YouTube video ID
        )
        return patterns.any { pattern ->
            Regex(pattern).matches(url)
        }
    }

    fun updateDestination(onSuccess: () -> Unit) {
        val state = _uiState.value
        val nameError = validateName(state.name)
        val addressError = validateAddress(state.address)
        val descriptionError = validateDescription(state.description)
        val urlLocationError = validateUrlLocation(state.urlLocation)
        val youtubeUrlError = validateYoutubeUrl(state.youtubeUrl) // Validasi YouTube URL

        _uiState.update {
            it.copy(
                nameError = nameError,
                addressError = addressError,
                descriptionError = descriptionError,
                urlLocationError = urlLocationError,
                youtubeUrlError = youtubeUrlError
            )
        }

        if (nameError != null || addressError != null || descriptionError != null ||
            urlLocationError != null || youtubeUrlError != null) {
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val result = destinationRepository.updateDestination(
                    categoryId = state.categoryId,
                    destinationId = state.destinationId,
                    name = state.name,
                    address = state.address,
                    description = state.description,
                    urlLocation = state.urlLocation,
                    //youtubeUrl = state.youtubeUrl.ifEmpty { null }, // Tambahkan YouTube URL
                    coverImageUri = if (state.isNewCoverSelected) state.coverImageUri else null,
                    pictureImageUris = state.pictureImageUris,
                    removedPictureIds = state.removedPictureIds
                )

                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true
                            )
                        }
                        delay(100)
                        onSuccess()
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    // Fungsi-fungsi lainnya tetap sama...
    private suspend fun loadCoverImage(destinationId: Int) {
        try {
            val bitmap = base64ImageService.getDestinationCoverImage(destinationId)
            _uiState.update {
                it.copy(
                    coverImage = bitmap,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadPictureImage(pictureId: Int) {
        try {
            val bitmap = base64ImageService.getPictureImage(pictureId)
            _uiState.update {
                val updatedPictureImages = it.pictureImages.toMutableMap()
                updatedPictureImages[pictureId] = bitmap
                it.copy(pictureImages = updatedPictureImages)
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    // Fungsi validasi
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Nama destinasi tidak boleh kosong"
            name.length < 3 -> "Nama destinasi minimal 3 karakter"
            name.length > 150 -> "Nama destinasi maksimal 150 karakter"
            else -> null
        }
    }

    private fun validateAddress(address: String): String? {
        return when {
            address.isBlank() -> "Alamat tidak boleh kosong"
            address.length < 5 -> "Alamat minimal 5 karakter"
            address.length > 150 -> "Alamat maksimal 150 karakter"
            else -> null
        }
    }

    private fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Deskripsi tidak boleh kosong"
            description.length < 10 -> "Deskripsi minimal 10 karakter"
            else -> null
        }
    }

    private fun validateUrlLocation(url: String): String? {
        return when {
            url.isBlank() -> "URL lokasi tidak boleh kosong"
            !url.contains("maps") && !url.contains("goo.gl") -> "URL harus berupa link Google Maps"
            url.length > 200 -> "URL maksimal 200 karakter"
            else -> null
        }
    }

    // Fungsi-fungsi update field lainnya
    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = validateName(name)
            )
        }
    }

    fun updateAddress(address: String) {
        _uiState.update {
            it.copy(
                address = address,
                addressError = validateAddress(address)
            )
        }
    }

    fun updateDescription(description: String) {
        _uiState.update {
            it.copy(
                description = description,
                descriptionError = validateDescription(description)
            )
        }
    }

    fun updateUrlLocation(urlLocation: String) {
        _uiState.update {
            it.copy(
                urlLocation = urlLocation,
                urlLocationError = validateUrlLocation(urlLocation)
            )
        }
    }

    fun setCoverImage(uri: Uri) {
        _uiState.update {
            it.copy(
                coverImageUri = uri,
                isNewCoverSelected = true
            )
        }
    }

    fun addPictureImage(uri: Uri) {
        val currentUris = _uiState.value.pictureImageUris
        if (currentUris.size < 3 - _uiState.value.pictures.size + _uiState.value.removedPictureIds.size) {
            _uiState.update {
                it.copy(pictureImageUris = currentUris + uri)
            }
        }
    }

    fun removePictureImage(uri: Uri) {
        val currentUris = _uiState.value.pictureImageUris
        _uiState.update {
            it.copy(pictureImageUris = currentUris.filter { it != uri })
        }
    }

    fun removePicture(pictureId: Int) {
        val currentRemovedIds = _uiState.value.removedPictureIds
        _uiState.update {
            it.copy(removedPictureIds = currentRemovedIds + pictureId)
        }
    }

    fun undoRemovePicture(pictureId: Int) {
        val currentRemovedIds = _uiState.value.removedPictureIds
        _uiState.update {
            it.copy(removedPictureIds = currentRemovedIds.filter { it != pictureId })
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}