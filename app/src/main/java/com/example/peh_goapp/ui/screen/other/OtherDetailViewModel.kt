package com.example.peh_goapp.ui.screen.other

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.OtherDetailModel
import com.example.peh_goapp.data.repository.OtherRepository
import com.example.peh_goapp.data.remote.api.ApiResult // FIX IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * State untuk layar Other detail
 */
data class OtherDetailUiState(
    val isLoading: Boolean = false,
    val isDeleteSuccess: Boolean = false,
    val otherDetail: OtherDetailModel? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class OtherDetailViewModel @Inject constructor(
    private val otherRepository: OtherRepository,
    private val tokenPreference: TokenPreference,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtherDetailUiState())
    val uiState: StateFlow<OtherDetailUiState> = _uiState.asStateFlow()

    fun loadOtherDetail(otherId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = otherRepository.getOtherById(otherId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otherDetail = result.data
                        )
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
        }
    }

    fun deleteOther(otherId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = otherRepository.deleteOther(otherId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isDeleteSuccess = true
                        )
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
        }
    }

    fun downloadBarcode(otherId: Int) {
        viewModelScope.launch {
            try {
                val token = tokenPreference.getToken()
                val url = "https://your-api-url.com/api/barcode/other/$otherId"

                withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $token")
                        .build()

                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val inputStream = response.body?.byteStream()
                        val file = File(context.getExternalFilesDir(null), "QR_Other_${otherId}.png")

                        inputStream?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Share atau buka file
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "image/png")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(Intent.createChooser(intent, "Open QR Code"))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Gagal mengunduh barcode: ${e.message}")
                }
            }
        }
    }

    fun isAdmin(): Boolean {
        return tokenPreference.getRole() == "ADMIN"
    }

    // ADD MISSING METHOD
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}