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
import com.example.peh_goapp.data.repository.StatsRepository
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
    val userRole: String = "",
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val youtubeUrl: String? = null, // URL YouTube asli dari database
    val youtubeEmbedUrl: String? = null, // URL untuk embed di WebView
)

@HiltViewModel
class DestinationDetailViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val favoriteRepository: FavoriteRepository,
    private val statsRepository: StatsRepository,
    private val tokenPreference: TokenPreference,
    private val base64ImageService: Base64ImageService
) : ViewModel() {

    private val TAG = "DestinationDetailViewModel"

    private val _uiState = MutableStateFlow(DestinationDetailUiState())
    val uiState: StateFlow<DestinationDetailUiState> = _uiState.asStateFlow()

    /**
     * Fungsi untuk mengkonversi YouTube URL ke format embed URL
     * Mendukung berbagai format: youtube.com/watch?v=, youtu.be/, youtube.com/embed/, atau hanya video ID
     */
    private fun convertYoutubeUrlToEmbedUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null

        Log.d(TAG, "Converting YouTube URL: $url")

        // Pattern untuk menangkap video ID dari berbagai format YouTube URL
        val patterns = listOf(
            // Format: youtube.com/watch?v=VIDEO_ID atau youtube.com/watch?v=VIDEO_ID&other_params
            Regex("(?:youtube\\.com/watch\\?v=)([^&\\n?#]+)"),
            // Format: youtu.be/VIDEO_ID
            Regex("(?:youtu\\.be/)([^&\\n?#]+)"),
            // Format: youtube.com/embed/VIDEO_ID (sudah embed format)
            Regex("(?:youtube\\.com/embed/)([^&\\n?#]+)"),
            // Format: hanya video ID (11 karakter)
            Regex("^([\\w-]{11})$")
        )

        for (pattern in patterns) {
            val matchResult = pattern.find(url)
            if (matchResult != null) {
                val videoId = matchResult.groupValues.getOrNull(1)
                if (!videoId.isNullOrBlank()) {
                    val embedUrl = "https://www.youtube.com/embed/$videoId"
                    Log.d(TAG, "Converted to embed URL: $embedUrl")
                    return embedUrl
                }
            }
        }

        Log.w(TAG, "Failed to convert YouTube URL: $url")
        return null
    }

    fun loadDestinationDetail(categoryId: Int, destinationId: Int) {
        val userRole = tokenPreference.getRole()
        val isAdmin = tokenPreference.isAdmin()

        Log.d(TAG, "Loading destination detail - Role: $userRole, IsAdmin: $isAdmin")

        _uiState.update {
            it.copy(
                isLoading = true,
                categoryId = categoryId,
                destinationId = destinationId,
                isAdmin = isAdmin,
                userRole = userRole
            )
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Memuat detail destinasi: $destinationId untuk role: $userRole")
                when (val result = destinationRepository.getDestinationDetail(categoryId, destinationId)) {
                    is ApiResult.Success -> {
                        val destination = result.data

                        // Konversi YouTube URL ke embed URL untuk WebView
                        val youtubeUrl = destination.youtubeUrl
                        val youtubeEmbedUrl = convertYoutubeUrlToEmbedUrl(youtubeUrl)

                        // Log untuk debugging
                        Log.d(TAG, "Original YouTube URL: $youtubeUrl")
                        Log.d(TAG, "Converted Embed URL: $youtubeEmbedUrl")

                        _uiState.update {
                            it.copy(
                                name = destination.name,
                                address = destination.address,
                                description = destination.description,
                                urlLocation = destination.urlLocation,
                                pictures = destination.pictures,
                                youtubeUrl = youtubeUrl, // Simpan URL asli
                                youtubeEmbedUrl = youtubeEmbedUrl, // Simpan URL embed untuk WebView
                                isLoading = false
                            )
                        }

                        // Load gambar dan data lainnya
                        loadCoverImage(destinationId)
                        destination.pictures.forEach { picture ->
                            loadPictureImage(picture.id)
                        }
                        recordDestinationView(destinationId)
                        checkFavoriteStatus(destinationId)

                        Log.d(TAG, "Berhasil memuat detail destinasi untuk ${if (isAdmin) "ADMIN" else "WISATAWAN"}")

                        // Log tambahan untuk debugging YouTube
                        if (!youtubeUrl.isNullOrEmpty()) {
                            Log.d(TAG, "Destinasi memiliki video YouTube: $youtubeUrl")
                            if (youtubeEmbedUrl != null) {
                                Log.d(TAG, "Video siap untuk ditampilkan dengan embed URL: $youtubeEmbedUrl")
                            } else {
                                Log.w(TAG, "Gagal mengkonversi YouTube URL ke embed format")
                            }
                        } else {
                            Log.d(TAG, "Destinasi tidak memiliki video YouTube")
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error loading destination detail: ${result.errorMessage}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadDestinationDetail: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    fun canDownloadQrCode(): Boolean {
        val canDownload = _uiState.value.isAdmin
        Log.d(TAG, "Can download QR Code: $canDownload (Role: ${_uiState.value.userRole})")
        return canDownload
    }

    fun validateAdminPermission(action: String): Boolean {
        val isAdmin = _uiState.value.isAdmin
        if (!isAdmin) {
            setErrorMessage("Akses ditolak. Fitur $action hanya tersedia untuk admin.")
            Log.w(TAG, "Admin permission denied for action: $action")
        }
        return isAdmin
    }

    private fun loadCoverImage(destinationId: Int) {
        viewModelScope.launch {
            try {
                val bitmap = base64ImageService.getDestinationCoverImage(destinationId)
                _uiState.update {
                    it.copy(coverImage = bitmap)
                }
                Log.d(TAG, "Cover image loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading cover image: ${e.message}", e)
            }
        }
    }

    private fun loadPictureImage(pictureId: Int) {
        viewModelScope.launch {
            try {
                val bitmap = base64ImageService.getPictureImage(pictureId)
                _uiState.update { currentState ->
                    currentState.copy(
                        pictureImages = currentState.pictureImages + (pictureId to bitmap)
                    )
                }
                Log.d(TAG, "Picture image $pictureId loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading picture image $pictureId: ${e.message}", e)
            }
        }
    }

    private fun recordDestinationView(destinationId: Int) {
        viewModelScope.launch {
            try {
                statsRepository.recordDestinationView(destinationId)
                Log.d(TAG, "View recorded for destination: $destinationId")
            } catch (e: Exception) {
                Log.e(TAG, "Error recording view: ${e.message}", e)
            }
        }
    }

    private fun checkFavoriteStatus(destinationId: Int) {
        viewModelScope.launch {
            try {
                when (val result = favoriteRepository.checkIsFavorite(destinationId)) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isFavorite = result.data)
                        }
                        Log.d(TAG, "Favorite status checked: ${result.data}")
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Error checking favorite status: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception checking favorite status: ${e.message}", e)
            }
        }
    }

    fun toggleFavorite() {
        val destinationId = _uiState.value.destinationId
        if (destinationId == 0) return

        viewModelScope.launch {
            try {
                when (val result = favoriteRepository.toggleFavorite(destinationId)) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isFavorite = result.data)
                        }
                        Log.d(TAG, "Favorite toggled successfully: ${result.data}")
                    }
                    is ApiResult.Error -> {
                        setErrorMessage("Gagal mengubah status favorit: ${result.errorMessage}")
                        Log.e(TAG, "Error toggling favorite: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                setErrorMessage("Terjadi kesalahan: ${e.message}")
                Log.e(TAG, "Exception saat toggle favorit: ${e.message}", e)
            }
        }
    }

    fun deleteDestination(onSuccess: () -> Unit) {
        if (!validateAdminPermission("hapus destinasi")) {
            return
        }

        val categoryId = _uiState.value.categoryId
        val destinationId = _uiState.value.destinationId

        viewModelScope.launch {
            try {
                when (val result = destinationRepository.deleteDestination(categoryId, destinationId)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "Destination deleted successfully")
                        onSuccess()
                    }
                    is ApiResult.Error -> {
                        setErrorMessage("Gagal menghapus destinasi: ${result.errorMessage}")
                        Log.e(TAG, "Error deleting destination: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                setErrorMessage("Terjadi kesalahan: ${e.message}")
                Log.e(TAG, "Exception deleting destination: ${e.message}", e)
            }
        }
    }

    fun setErrorMessage(message: String) {
        _uiState.update {
            it.copy(errorMessage = message)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}