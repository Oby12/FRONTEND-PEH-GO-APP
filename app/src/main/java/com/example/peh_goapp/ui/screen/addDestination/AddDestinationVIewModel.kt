package com.example.peh_goapp.ui.screen.adddestination

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.remote.api.ApiResult
import com.example.peh_goapp.data.repository.DestinationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * State untuk layar tambah destinasi
 */
data class AddDestinationUiState(
    val categoryId: Int = 0,
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val urlLocation: String = "",
    val coverImageUri: Uri? = null,
    val pictureImageUris: List<Uri> = emptyList(),
    val nameError: String? = null,
    val addressError: String? = null,
    val descriptionError: String? = null,
    val urlLocationError: String? = null,
    val youtubeUrl: String = "", // Field baru
    val youtubeUrlError: String? = null, // Field baru untuk error
    val coverError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk mengelola layar tambah destinasi
 */
@HiltViewModel
class AddDestinationViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddDestinationUiState())
    val uiState: StateFlow<AddDestinationUiState> = _uiState.asStateFlow()

    fun updateYoutubeUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            youtubeUrl = url,
            youtubeUrlError = validateYoutubeUrl(url)
        )
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

//    fun submitDestination() {
//        viewModelScope.launch {
//            // Validasi semua field
//            val hasError = validateAllFields()
//            if (hasError) return@launch
//
//            _uiState.value = _uiState.value.copy(isLoading = true)
//
//            try {
//                // Prepare multipart data dengan YouTube URL
//                val result = repository.addDestination(
//                    categoryId = _uiState.value.categoryId,
//                    name = _uiState.value.name,
//                    address = _uiState.value.address,
//                    description = _uiState.value.description,
//                    urlLocation = _uiState.value.urlLocation,
//                    youtubeUrl = _uiState.value.youtubeUrl.ifEmpty { null },
//                    coverImageUri = _uiState.value.coverImageUri!!,
//                    pictureUris = _uiState.value.pictureUris
//                )
//
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    isSuccess = true
//                )
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    errorMessage = e.message
//                )
//            }
//        }
//    }





    /**
     * Mengatur category ID yang sedang aktif
     */
    fun setCategoryId(categoryId: Int) {
        _uiState.update { it.copy(categoryId = categoryId) }
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
     * Menetapkan gambar cover (utama)
     */
    fun setCoverImage(uri: Uri) {
        _uiState.update {
            it.copy(
                coverImageUri = uri,
                coverError = null
            )
        }
    }

    /**
     * Menambahkan gambar ke daftar gambar (maksimal 3)
     */
    fun addPictureImage(uri: Uri) {
        val currentPictures = _uiState.value.pictureImageUris
        if (currentPictures.size < 3) {
            _uiState.update {
                it.copy(pictureImageUris = currentPictures + uri)
            }
        }
    }

    /**
     * Menghapus gambar dari daftar gambar
     */
    fun removePictureImage(uri: Uri) {
        val currentPictures = _uiState.value.pictureImageUris
        _uiState.update {
            it.copy(pictureImageUris = currentPictures.filter { it != uri })
        }
    }

    /**
     * Menambahkan destinasi ke server
     */
    fun addDestination(onSuccess: () -> Unit) {
        // Validasi input
        val state = _uiState.value
        val nameError = validateName(state.name)
        val addressError = validateAddress(state.address)
        val descriptionError = validateDescription(state.description)
        val urlLocationError = validateUrlLocation(state.urlLocation)
        val coverError = validateCover(state.coverImageUri)

        // Update state dengan error jika ada
        _uiState.update {
            it.copy(
                nameError = nameError,
                addressError = addressError,
                descriptionError = descriptionError,
                urlLocationError = urlLocationError,
                coverError = coverError
            )
        }

        // Cek apakah ada error
        if (nameError != null || addressError != null || descriptionError != null ||
            urlLocationError != null || coverError != null) {
            return
        }

        // Mulai proses penambahan destinasi
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val result = destinationRepository.addDestination(
                    categoryId = state.categoryId,
                    name = state.name,
                    address = state.address,
                    description = state.description,
                    urlLocation = state.urlLocation,
                    youtubeUrl = state.youtubeUrl.ifEmpty { null }, //revisi add video youtube
                    coverImageUri = state.coverImageUri!!,
                    pictureImageUris = if (state.pictureImageUris.isEmpty()) null else state.pictureImageUris
                )

                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                errorMessage = null
                            )
                        }
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

    private fun validateCover(uri: Uri?): String? {
        return when {
            uri == null -> "Gambar cover harus dipilih"
            else -> null
        }
    }
}