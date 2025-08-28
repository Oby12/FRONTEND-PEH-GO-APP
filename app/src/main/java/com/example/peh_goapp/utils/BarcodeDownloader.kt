package com.example.peh_goapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Utility class untuk download/save barcode ke storage
 */
object BarcodeDownloader {
    private const val TAG = "BarcodeDownloader"

    /**
     * Simpan bitmap barcode ke gallery/storage
     */
    fun saveQrCodeToGallery(
        context: Context,
        bitmap: Bitmap,
        filename: String = "QR_Code_${System.currentTimeMillis()}.png"
    ): Boolean {
        return try {
            val outputStream: OutputStream?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ menggunakan Scoped Storage
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                // Android 9 dan sebelumnya
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(imagesDir, filename)
                outputStream = FileOutputStream(imageFile)
            }

            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            Log.d(TAG, "QR Code berhasil disimpan: $filename")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving QR code: ${e.message}", e)
            false
        }
    }

    /**
     * Simpan QR code untuk Other
     */
    fun saveOtherQrCode(context: Context, bitmap: Bitmap, otherId: Int, otherName: String): Boolean {
        val sanitizedName = otherName.replace("[^a-zA-Z0-9\\s]".toRegex(), "").replace("\\s+".toRegex(), "_")
        val filename = "QR_Other_${otherId}_${sanitizedName}.png"
        return saveQrCodeToGallery(context, bitmap, filename)
    }

    /**
     * Simpan QR code untuk Destination
     */
    fun saveDestinationQrCode(
        context: Context,
        bitmap: Bitmap,
        categoryId: Int,
        destinationId: Int,
        destinationName: String
    ): Boolean {
        val sanitizedName = destinationName.replace("[^a-zA-Z0-9\\s]".toRegex(), "").replace("\\s+".toRegex(), "_")
        val filename = "QR_Destination_${categoryId}_${destinationId}_${sanitizedName}.png"
        return saveQrCodeToGallery(context, bitmap, filename)
    }
}