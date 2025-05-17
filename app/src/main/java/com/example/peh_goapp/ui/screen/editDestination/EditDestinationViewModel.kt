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

/**
 * State untuk layar edit destinasi
 */
data class EditDestinationUiState(
    val isLoading: Boolean = true,
    val categoryId: Int = 0,
    val destinationId: Int = 0,
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val urlLocation: String = "",
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
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk mengelola layar edit destinasi
 */
@HiltViewModel
class EditDestinationViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val base64ImageService: Base64ImageService
) : ViewModel() {

    private val TAG = "EditDestinationVM"
    private val _uiState = MutableStateFlow(EditDestinationUiState())
    val uiState: StateFlow<EditDestinationUiState> = _uiState.asStateFlow()

    /**
     * Memuat detail destinasi untuk diedit
     */
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

    /**
     * Memuat gambar cover
     */
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

    /**
     * Memuat gambar tambahan
     */
    private suspend fun loadPictureImage(pictureId: Int) {
        try {
            val bitmap = base64ImageService.getPictureImage(pictureId)
            _uiState.update {
                val updatedPictureImages = it.pictureImages.toMutableMap()
                updatedPictureImages[pictureId] = bitmap
                it.copy(pictureImages = updatedPictureImages)
            }
        } catch (e: Exception) {
            // Tetap lanjutkan meskipun ada gambar yang gagal dimuat
        }
    }

    /**
     * Memperbarui nama destinasi
     */
    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = validateName(name)
            )
        }
    }

    /**
     * Memperbarui alamat destinasi
     */
    fun updateAddress(address: String) {
        _uiState.update {
            it.copy(
                address = address,
                addressError = validateAddress(address)
            )
        }
    }

    /**
     * Memperbarui deskripsi destinasi
     */
    fun updateDescription(description: String) {
        _uiState.update {
            it.copy(
                description = description,
                descriptionError = validateDescription(description)
            )
        }
    }

    /**
     * Memperbarui URL lokasi
     */
    fun updateUrlLocation(urlLocation: String) {
        _uiState.update {
            it.copy(
                urlLocation = urlLocation,
                urlLocationError = validateUrlLocation(urlLocation)
            )
        }
    }

    /**
     * Mengubah gambar cover
     */
    fun setCoverImage(uri: Uri) {
        _uiState.update {
            it.copy(
                coverImageUri = uri,
                isNewCoverSelected = true
            )
        }
    }

    /**
     * Menambahkan gambar tambahan
     */
    fun addPictureImage(uri: Uri) {
        val currentUris = _uiState.value.pictureImageUris
        if (currentUris.size < 3 - _uiState.value.pictures.size + _uiState.value.removedPictureIds.size) {
            _uiState.update {
                it.copy(pictureImageUris = currentUris + uri)
            }
        }
    }

    /**
     * Menghapus gambar tambahan baru
     */
    fun removePictureImage(uri: Uri) {
        val currentUris = _uiState.value.pictureImageUris
        _uiState.update {
            it.copy(pictureImageUris = currentUris.filter { it != uri })
        }
    }

    /**
     * Menghapus gambar tambahan yang sudah ada
     */
    fun removePicture(pictureId: Int) {
        val currentRemovedIds = _uiState.value.removedPictureIds
        _uiState.update {
            it.copy(removedPictureIds = currentRemovedIds + pictureId)
        }

        // Log untuk debug
        Log.d(TAG, "Menandai gambar dengan ID $pictureId untuk dihapus")
        Log.d(TAG, "Total removedPictureIds: ${_uiState.value.removedPictureIds}")
    }

    /**
     * Membatalkan penghapusan gambar tambahan yang sudah ada
     */
    fun undoRemovePicture(pictureId: Int) {
        val currentRemovedIds = _uiState.value.removedPictureIds
        _uiState.update {
            it.copy(removedPictureIds = currentRemovedIds.filter { it != pictureId })
        }

        // Log untuk debug
        Log.d(TAG, "Membatalkan penghapusan gambar dengan ID $pictureId")
        Log.d(TAG, "Total removedPictureIds: ${_uiState.value.removedPictureIds}")
    }

    /**
     * Menyimpan perubahan pada destinasi
     */
    fun updateDestination(onSuccess: () -> Unit) {
        // Validasi input
        val state = _uiState.value
        val nameError = validateName(state.name)
        val addressError = validateAddress(state.address)
        val descriptionError = validateDescription(state.description)
        val urlLocationError = validateUrlLocation(state.urlLocation)

        // Update state dengan error jika ada
        _uiState.update {
            it.copy(
                nameError = nameError,
                addressError = addressError,
                descriptionError = descriptionError,
                urlLocationError = urlLocationError
            )
        }

        // Cek apakah ada error
        if (nameError != null || addressError != null || descriptionError != null || urlLocationError != null) {
            return
        }

        // Log informasi untuk debugging
        Log.d(TAG, "Memulai proses update destinasi")
        Log.d(TAG, "CategoryId: ${state.categoryId}, DestinationId: ${state.destinationId}")
        Log.d(TAG, "Cover baru dipilih: ${state.isNewCoverSelected}")
        Log.d(TAG, "Jumlah gambar baru: ${state.pictureImageUris.size}")
        Log.d(TAG, "Gambar yang akan dihapus: ${state.removedPictureIds}")

        // Mulai proses update destinasi
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Buat request update dengan semua data yang diperlukan
                val result = destinationRepository.updateDestination(
                    categoryId = state.categoryId,
                    destinationId = state.destinationId,
                    name = state.name,
                    address = state.address,
                    description = state.description,
                    urlLocation = state.urlLocation,
                    coverImageUri = if (state.isNewCoverSelected) state.coverImageUri else null,
                    pictureImageUris = if (state.pictureImageUris.isEmpty()) null else state.pictureImageUris,
                    removedPictureIds = if (state.removedPictureIds.isEmpty()) null else state.removedPictureIds
                )

                when (result) {
                    is ApiResult.Success -> {
                        // Delay sedikit untuk memastikan tampilan loading terlihat
                        delay(500)

                        // Update state sebelum navigasi
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                errorMessage = null
                            )
                        }

                        // Panggil callback untuk navigasi
                        onSuccess()
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error update: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat update: ${e.message}", e)
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
     * Menghapus pesan error
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Fungsi-fungsi validasi
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Nama destinasi tidak boleh kosong"
            name.length > 150 -> "Nama destinasi maksimal 150 karakter"
            else -> null
        }
    }

    private fun validateAddress(address: String): String? {
        return when {
            address.isBlank() -> "Alamat destinasi tidak boleh kosong"
            address.length > 150 -> "Alamat destinasi maksimal 150 karakter"
            else -> null
        }
    }

    private fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Deskripsi destinasi tidak boleh kosong"
            else -> null
        }
    }

    private fun validateUrlLocation(urlLocation: String): String? {
        return when {
            urlLocation.isBlank() -> "URL lokasi tidak boleh kosong"
            urlLocation.length > 200 -> "URL lokasi maksimal 200 karakter"
            !urlLocation.startsWith("http") -> "URL lokasi harus diawali dengan http:// atau https://"
            else -> null
        }
    }
}