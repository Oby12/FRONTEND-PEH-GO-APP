package com.example.peh_goapp.ui.screen.other

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.repository.OtherRepository
import com.example.peh_goapp.data.remote.api.ApiResult // FIX IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State untuk layar Add Other
 */
data class AddOtherUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val name: String = "",
    val category: String = "",
    val story: String = "",
    val coverImageUri: Uri? = null,
    val nameError: String? = null,
    val categoryError: String? = null,
    val storyError: String? = null,
    val coverImageError: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AddOtherViewModel @Inject constructor(
    private val otherRepository: OtherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddOtherUiState())
    val uiState: StateFlow<AddOtherUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = validateName(name)
            )
        }
    }

    fun updateCategory(category: String) {
        _uiState.update {
            it.copy(
                category = category,
                categoryError = validateCategory(category)
            )
        }
    }

    fun updateStory(story: String) {
        _uiState.update {
            it.copy(
                story = story,
                storyError = validateStory(story)
            )
        }
    }

    fun updateCoverImage(uri: Uri) {
        _uiState.update {
            it.copy(
                coverImageUri = uri,
                coverImageError = validateCoverImage(uri)
            )
        }
    }

    fun createOther(onSuccess: () -> Unit) {
        val state = uiState.value

        // Validasi semua field
        val nameError = validateName(state.name)
        val categoryError = validateCategory(state.category)
        val storyError = validateStory(state.story)
        val coverImageError = validateCoverImage(state.coverImageUri)

        _uiState.update {
            it.copy(
                nameError = nameError,
                categoryError = categoryError,
                storyError = storyError,
                coverImageError = coverImageError
            )
        }

        // Jika ada error validasi, hentikan proses
        if (nameError != null || categoryError != null || storyError != null || coverImageError != null) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val result = otherRepository.createOther(
                    name = state.name,
                    category = state.category,
                    story = state.story,
                    coverImageUri = state.coverImageUri!!
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Validation functions
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Nama tidak boleh kosong"
            name.length > 150 -> "Nama maksimal 150 karakter"
            else -> null
        }
    }

    private fun validateCategory(category: String): String? {
        return when {
            category.isBlank() -> "Kategori tidak boleh kosong"
            category.length > 100 -> "Kategori maksimal 100 karakter"
            else -> null
        }
    }

    private fun validateStory(story: String): String? {
        return when {
            story.isBlank() -> "Story tidak boleh kosong"
            else -> null
        }
    }

    private fun validateCoverImage(uri: Uri?): String? {
        return when {
            uri == null -> "Gambar cover harus dipilih"
            else -> null
        }
    }
}