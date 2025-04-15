package com.example.peh_goapp.ui.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.R
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

data class MainScreenState(
    val isLoading: Boolean = false,
    val banners: List<BannerModel> = emptyList(),
    val categories: List<CategoryModel> = emptyList(),
    val isDrawerOpen: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    init {
        loadBanners()
        loadCategories()
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

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = userRepository.logout()) {
                is com.example.peh_goapp.data.remote.api.ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onLogoutSuccess()
                }
                is com.example.peh_goapp.data.remote.api.ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.errorMessage
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}