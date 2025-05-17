package com.example.peh_goapp.ui.screen.scanner

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * State untuk layar scanner QR code
 */
data class ScannerUiState(
    val isLoading: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val isTorchOn: Boolean = false,
    val scanResult: String? = null,
    val errorMessage: String? = null,
    val parsedData: ParsedQrData? = null
)

/**
 * Data yang diuraikan dari QR code
 */
data class ParsedQrData(
    val categoryId: Int,
    val destinationId: Int
)

/**
 * ViewModel untuk layar scanner QR code
 */
@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {
    private val TAG = "ScannerViewModel"

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    /**
     * Mengatur status izin kamera
     */
    fun setCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    /**
     * Mengaktifkan/menonaktifkan flash kamera
     */
    fun toggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    /**
     * Memproses hasil scan QR code
     * Format QR code: pehgo://destination/{categoryId}/{destinationId}
     */
    fun processScanResult(result: String) {
        Log.d(TAG, "Processing scan result: $result")
        _uiState.update { it.copy(scanResult = result) }

        try {
            val pattern = Pattern.compile("pehgo://destination/(\\d+)/(\\d+)")
            val matcher = pattern.matcher(result)

            if (matcher.find()) {
                val categoryId = matcher.group(1)?.toIntOrNull()
                val destinationId = matcher.group(2)?.toIntOrNull()

                if (categoryId != null && destinationId != null) {
                    Log.d(TAG, "Parsed QR data: categoryId=$categoryId, destinationId=$destinationId")
                    _uiState.update {
                        it.copy(
                            parsedData = ParsedQrData(categoryId, destinationId),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = "QR code tidak valid",
                            parsedData = null
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Format QR code tidak dikenali",
                        parsedData = null
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR data: ${e.message}")
            _uiState.update {
                it.copy(
                    errorMessage = "Error: ${e.message}",
                    parsedData = null
                )
            }
        }
    }

    /**
     * Clear scan result dan error
     */
    fun resetScan() {
        _uiState.update {
            it.copy(
                scanResult = null,
                errorMessage = null,
                parsedData = null
            )
        }
    }
}