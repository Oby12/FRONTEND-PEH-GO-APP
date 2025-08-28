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
    val categoryId: Int? = null,
    val destinationId: Int? = null,
    val otherId: Int? = null, // ADD THIS FIELD
    val type: String // "destination" atau "other"
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
    // Update processScanResult method
// UPDATE processScanResult method
    fun processScanResult(result: String) {
        if (uiState.value.parsedData != null) {
            Log.d(TAG, "Hasil scan sudah diproses, mengabaikan pindaian baru.")
            return
        }

        Log.d(TAG, "Memproses hasil scan: $result")

        try {
            val parsedData = when {
                // Format untuk destinasi: pehgo://destination/{categoryId}/{destinationId}
                result.startsWith("pehgo://destination/") -> {
                    val parts = result.removePrefix("pehgo://destination/").split("/")
                    if (parts.size == 2) {
                        val categoryId = parts[0].toIntOrNull()
                        val destinationId = parts[1].toIntOrNull()

                        if (categoryId != null && destinationId != null) {
                            ParsedQrData(
                                categoryId = categoryId,
                                destinationId = destinationId,
                                type = "destination"
                            )
                        } else null
                    } else null
                }

                // FORMAT UNTUK OTHER: pehgo://other/{otherId}
                result.startsWith("pehgo://other/") -> {
                    val otherIdStr = result.removePrefix("pehgo://other/")
                    val otherId = otherIdStr.toIntOrNull()

                    if (otherId != null) {
                        ParsedQrData(
                            otherId = otherId,
                            type = "other"
                        )
                    } else null
                }

                else -> null
            }

            if (parsedData != null) {
                _uiState.update {
                    it.copy(
                        scanResult = result,
                        parsedData = parsedData,
                        errorMessage = null
                    )
                }
                Log.d(TAG, "QR code berhasil diuraikan: $parsedData")
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Format QR code tidak valid"
                    )
                }
                Log.e(TAG, "Format QR code tidak valid: $result")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saat menguraikan QR code: ${e.message}")
            _uiState.update {
                it.copy(
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    // ADD MISSING clearError METHOD
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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