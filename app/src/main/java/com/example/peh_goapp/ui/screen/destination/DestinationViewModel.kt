package com.example.peh_goapp.ui.screen.destination

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.model.DestinationModel
import com.example.peh_goapp.data.remote.api.ApiConfig
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
 * State UI untuk halaman daftar destinasi
 */
data class DestinationUiState(
    val isLoading: Boolean = false,
    val destinations: List<DestinationModel> = emptyList(),
    val searchQuery: String = "",
    val filteredDestinations: List<DestinationModel> = emptyList(),
    val errorMessage: String? = null,
    val categoryId: Int = 0,
    val categoryName: String = "Destinasi"
)

/**
 * ViewModel untuk mengelola data dan logika halaman daftar destinasi
 */
@HiltViewModel
class DestinationViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val base64ImageService: Base64ImageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DestinationUiState())
    val uiState: StateFlow<DestinationUiState> = _uiState.asStateFlow()

    /**
     * Mengatur ID kategori yang aktif dan memuat destinasi berdasarkan kategori tersebut
     */
    fun setCategoryId(categoryId: Int, categoryName: String = "") {
        _uiState.update {
            it.copy(
                categoryId = categoryId,
                categoryName = if (categoryName.isNotEmpty()) categoryName else getCategoryNameById(
                    categoryId
                )
            )
        }
        loadDestinations(categoryId)
    }

    /**
     * Mendapatkan nama kategori berdasarkan ID
     */
    private fun getCategoryNameById(id: Int): String {
        return when (id) {
            1 -> "Tour"
            2 -> "Hotel"
            3 -> "Transportation"
            4 -> "Culinary"
            5 -> "Mall"
            6 -> "Souvenir"
            else -> "Destinasi"
        }
    }

    /**
     * Memuat data destinasi berdasarkan kategori
     */
    private fun loadDestinations(categoryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Bersihkan cache gambar terlebih dahulu untuk memastikan data baru
                base64ImageService.clearCache()

                // Gunakan API untuk mengambil data
                val result = destinationRepository.getDestinations(categoryId)

                when (result) {
                    is ApiResult.Success -> {
                        val destinations = result.data
                        Log.d(
                            "DestinationViewModel",
                            "Berhasil memuat ${destinations.size} destinasi"
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                destinations = destinations,
                                filteredDestinations = destinations,
                                errorMessage = null
                            )
                        }
                    }

                    is ApiResult.Error -> {
                        Log.e(
                            "DestinationViewModel",
                            "Error loading destinations: ${result.errorMessage}"
                        )

                        // Jika terjadi error, gunakan data dummy sebagai fallback
                        val dummyData = getDummyData(categoryId)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                destinations = dummyData,
                                filteredDestinations = dummyData,
                                errorMessage = "Terjadi kesalahan: ${result.errorMessage}. Menampilkan data contoh."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DestinationViewModel", "Exception loading destinations: ${e.message}", e)

                // Jika terjadi exception, gunakan data dummy sebagai fallback
                val dummyData = getDummyData(categoryId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        destinations = dummyData,
                        filteredDestinations = dummyData,
                        errorMessage = "Terjadi kesalahan: ${e.message}. Menampilkan data contoh."
                    )
                }
            }
        }
    }

    /**
     * Menyediakan data dummy untuk keperluan pengembangan dan testing
     */
    private fun getDummyData(categoryId: Int): List<DestinationModel> {
        // Daftar destinasi untuk testing UI
        val baseDummies = listOf(
            DestinationModel(
                id = 1,
                name = "Jakabaring Sport City",
                address = "Jakabaring",
                description = "Komplek olahraga terbesar di Sumatera Selatan",
                urlLocation = "https://maps.app.goo.gl/1",
                coverUrl = "/api/images/base64/covers/1" // Gunakan endpoint base64
            ),
            DestinationModel(
                id = 2,
                name = "Museum Balaputra Dewa",
                address = "Sukaramai",
                description = "Museum sejarah Palembang",
                urlLocation = "https://maps.app.goo.gl/2",
                coverUrl = "/api/images/base64/covers/2"
            ),
            DestinationModel(
                id = 3,
                name = "Kopi 16",
                address = "Gedung Pasar 16",
                description = "Kedai kopi terkenal di Palembang",
                urlLocation = "https://maps.app.goo.gl/3",
                coverUrl = "/api/images/base64/covers/3"
            ),
            DestinationModel(
                id = 4,
                name = "Benteng Kuto Besak",
                address = "Sungai Musi",
                description = "Benteng peninggalan sejarah di tepi Sungai Musi",
                urlLocation = "https://maps.app.goo.gl/4",
                coverUrl = "/api/images/base64/covers/4"
            ),
            DestinationModel(
                id = 5,
                name = "Museum Bayt Al-Qur'an Al-akbar",
                address = "Gandus",
                description = "Museum dengan Al-Qur'an ukiran kayu terbesar",
                urlLocation = "https://maps.app.goo.gl/5",
                coverUrl = "/api/images/base64/covers/5"
            )
        )

        // Tambahkan prefiks berdasarkan kategori
        val categoryPrefix = when (categoryId) {
            1 -> "Wisata "
            2 -> "Hotel "
            3 -> "Transportasi "
            4 -> "Kuliner "
            5 -> "Mall "
            6 -> "Oleh-oleh "
            else -> ""
        }

        // Untuk kategori selain 1, tambahkan prefiks pada nama destinasi
        return if (categoryId == 1) {
            baseDummies
        } else {
            baseDummies.mapIndexed { index, destination ->
                destination.copy(
                    id = index + 1,
                    name = "$categoryPrefix ${index + 1}",
                    address = "Lokasi $categoryPrefix ${index + 1}"
                )
            }
        }
    }

    /**
     * Memperbarui query pencarian dan memfilter destinasi berdasarkan query
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isEmpty()) {
                currentState.destinations
            } else {
                currentState.destinations.filter { destination ->
                    destination.name.contains(query, ignoreCase = true) ||
                            destination.address.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredDestinations = filtered
            )
        }
    }

    /**
     * Menghapus pesan error dari state
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Menyegarkan data destinasi
     */
    fun refreshDestinations() {
        loadDestinations(_uiState.value.categoryId)
    }
}