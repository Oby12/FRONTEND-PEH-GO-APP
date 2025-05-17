package com.example.peh_goapp.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.example.peh_goapp.data.remote.api.ApiConfig
import java.io.File

/**
 * Utility class untuk operasi terkait Destinasi
 */
object DestinationUtils {

    /**
     * Download QR Code untuk destinasi menggunakan DownloadManager
     * @param context Context Android
     * @param categoryId ID kategori destinasi
     * @param destinationId ID destinasi
     * @param destinationName Nama destinasi untuk nama file
     * @return ID download dari DownloadManager
     */
    fun downloadQrCode(
        context: Context,
        categoryId: Int,
        destinationId: Int,
        destinationName: String
    ): Long {
        try {
            // Buat URL untuk download QR code
            val barcodeUrl = "${ApiConfig.BASE_URL}api/barcode/$categoryId/$destinationId"

            // Sanitasi nama destinasi untuk nama file
            val sanitizedName = destinationName.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "${sanitizedName}_qrcode.png"

            // Buat request download
            val request = DownloadManager.Request(Uri.parse(barcodeUrl))
                .setTitle("Download QR Code")
                .setDescription("Mengunduh QR Code untuk $destinationName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            // Tambahkan header otorisasi jika diperlukan
            // request.addRequestHeader("Authorization", tokenPreference.getToken())

            // Dapatkan layanan DownloadManager
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            // Mulai download dan dapatkan ID download
            val downloadId = downloadManager.enqueue(request)

            // Tampilkan pesan
            Toast.makeText(
                context,
                "QR Code sedang diunduh. Lihat notifikasi untuk detail.",
                Toast.LENGTH_LONG
            ).show()

            return downloadId
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal mengunduh QR Code: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            throw e
        }
    }
}