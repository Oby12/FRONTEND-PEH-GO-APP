package com.example.peh_goapp.ui.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.R
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.BannerModel
import com.example.peh_goapp.data.model.CategoryModel
import com.example.peh_goapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainViewModel"

data class MainScreenState(
    val isLoading: Boolean = false,
    val banners: List<BannerModel> = emptyList(),
    val categories: List<CategoryModel> = emptyList(),
    val isDrawerOpen: Boolean = false,
    val userName: String = "",
    val errorMessage: String? = null,
    val logoutSuccess: Boolean = false,
    // PERBAIKAN: tambahkan flag untuk menentukan apakah error perlu ditampilkan
    val shouldShowError: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenPreference: TokenPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    init {
        loadBanners()
        loadCategories()
        loadUserData()
    }

    private fun loadUserData() {
        // Debugging untuk token dan data pengguna yang tersimpan
        val token = tokenPreference.getToken()
        val name = tokenPreference.getName()
        val username = tokenPreference.getUsername()
        val email = tokenPreference.getEmail()
        val role = tokenPreference.getRole()

        Log.d(TAG, "Token: ${if(token.isBlank()) "TIDAK ADA" else "${token.take(10)}..."}")
        Log.d(TAG, "Nama tersimpan: '$name'")
        Log.d(TAG, "Username tersimpan: '$username'")
        Log.d(TAG, "Email tersimpan: '$email'")
        Log.d(TAG, "Role tersimpan: '$role'")

        // Mendapatkan nama pengguna dari repository (dengan fallback)
        val userName = userRepository.getUserName()
        Log.d(TAG, "Hasil getUserName(): '$userName'")

        _uiState.update { it.copy(userName = userName) }
    }

    private fun loadBanners() {
        val banners = listOf(
            BannerModel(
                id = 1,
                imageUrl = R.drawable.slider_satu,
                title = "Promo Hotel 20%",
                description = "Santika Hotel"
            ),
            BannerModel(
                id = 2,
                imageUrl = R.drawable.slider_dua,
                title = "Wisata Gunung",
                description = "Indahnya Alam Indonesia"
            ),
            BannerModel(
                id = 3,
                imageUrl = R.drawable.slider_tiga,
                title = "Kuliner Khas",
                description = "Cicipi Makanan Tradisional"
            )
        )

        _uiState.update { it.copy(banners = banners) }
    }

    private fun loadCategories() {
        // Kategori disusun sesuai dengan urutan pada desain
        val categories = listOf(
            CategoryModel(
                id = 1,
                name = "Tour",
                iconResId = R.drawable.destination
            ),
            CategoryModel(
                id = 2,
                name = "Hotel",
                iconResId = R.drawable.hotel
            ),
            CategoryModel(
                id = 3,
                name = "Transportation",
                iconResId = R.drawable.transportation
            ),
            CategoryModel(
                id = 4,
                name = "Culinary",
                iconResId = R.drawable.culinary
            ),
            CategoryModel(
                id = 5,
                name = "Mall",
                iconResId = R.drawable.mall
            ),
            CategoryModel(
                id = 6,
                name = "Souvenir",
                iconResId = R.drawable.souvenir
            )
        )

        _uiState.update { it.copy(categories = categories) }
    }

    fun toggleDrawer() {
        _uiState.update { it.copy(isDrawerOpen = !it.isDrawerOpen) }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d(TAG, "Memulai proses logout...")
            _uiState.update { it.copy(isLoading = true) }

            try {
                when (val result = userRepository.logout()) {
                    is com.example.peh_goapp.data.remote.api.ApiResult.Success -> {
                        Log.d(TAG, "Logout berhasil: ${result.data}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                logoutSuccess = true,
                                // PERBAIKAN: Jangan tampilkan error pada kasus sukses
                                errorMessage = null,
                                shouldShowError = false
                            )
                        }
                    }
                    is com.example.peh_goapp.data.remote.api.ApiResult.Error -> {
                        Log.e(TAG, "Logout error: ${result.errorMessage}")

                        // PERBAIKAN: Set errorMessage tapi jangan tampilkan jika logout lokal berhasil
                        // (UserRepository seharusnya sudah menangani ini)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                // Simpan error, tapi tidak perlu menampilkannya ke user
                                errorMessage = result.errorMessage,
                                // Tetap anggap sukses, karena logout lokal berhasil
                                logoutSuccess = true,
                                // PERBAIKAN: Jangan tampilkan error
                                shouldShowError = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat logout: ${e.message}", e)

                // PERBAIKAN: Bahkan jika ada exception, logout lokal tetap berhasil
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}",
                        logoutSuccess = true,
                        shouldShowError = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearLogoutSuccess() {
        _uiState.update { it.copy(logoutSuccess = false) }
    }

    fun refreshUserData() {
        loadUserData()
    }
}