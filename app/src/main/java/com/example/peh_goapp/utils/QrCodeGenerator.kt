// File: util/QrCodeGenerator.kt
package com.example.peh_goapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.*

/**
 * Utility class untuk generate QR code
 */
object QrCodeGenerator {
    private const val TAG = "QrCodeGenerator"

    /**
     * Generate QR code bitmap dari string content
     */
    fun generateQrCode(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1

            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            Log.d(TAG, "QR code berhasil di-generate untuk: $content")
            bitmap

        } catch (e: WriterException) {
            Log.e(TAG, "Error generating QR code: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error generating QR code: ${e.message}", e)
            null
        }
    }

    /**
     * Generate QR code untuk Other dengan format khusus
     */
    fun generateOtherQrCode(otherId: Int): Bitmap? {
        val content = "pehgo://other/$otherId"
        return generateQrCode(content)
    }

    /**
     * Generate QR code untuk Destination (untuk konsistensi)
     */
    fun generateDestinationQrCode(categoryId: Int, destinationId: Int): Bitmap? {
        val content = "pehgo://destination/$categoryId/$destinationId"
        return generateQrCode(content)
    }
}