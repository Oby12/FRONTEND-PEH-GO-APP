package com.example.peh_goapp.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.ApiConfig
import java.io.File

/**
 * Utility class untuk operasi terkait Destinasi
 */
object DestinationUtils {

    /**
     * Download QR Code untuk destinasi menggunakan DownloadManager dengan authorization
     * HANYA untuk role ADMIN
     * @param context Context Android
     * @param categoryId ID kategori destinasi
     * @param destinationId ID destinasi
     * @param destinationName Nama destinasi untuk nama file
     * @param tokenPreference Token preference untuk authorization
     * @return ID download dari DownloadManager
     */
    fun downloadQrCode(
        context: Context,
        categoryId: Int,
        destinationId: Int,
        destinationName: String,
        tokenPreference: TokenPreference? = null
    ): Long {
        try {
            // Validasi role admin jika tokenPreference tersedia
            tokenPreference?.let {
                if (!it.isAdmin()) {
                    Toast.makeText(
                        context,
                        "Akses ditolak. Fitur download QR Code hanya untuk admin.",
                        Toast.LENGTH_LONG
                    ).show()
                    throw SecurityException("Unauthorized: Admin access required")
                }
            }

            // Buat URL untuk download QR code
            val barcodeUrl = "${ApiConfig.BASE_URL}api/barcode/$categoryId/$destinationId"

            // Sanitasi nama destinasi untuk nama file
            val sanitizedName = destinationName.replace(Regex("[^a-zA-Z0-9\\s]"), "_")
            val fileName = "${sanitizedName}_qrcode.png"

            // Buat request download
            val request = DownloadManager.Request(Uri.parse(barcodeUrl))
                .setTitle("Download QR Code")
                .setDescription("Mengunduh QR Code untuk $destinationName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            // Tambahkan header otorisasi untuk admin
            tokenPreference?.let {
                val token = it.getToken()
                if (token.isNotBlank()) {
                    request.addRequestHeader("Authorization", token)
                }
            }

            // Dapatkan layanan DownloadManager
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            // Mulai download dan dapatkan ID download
            val downloadId = downloadManager.enqueue(request)

            // Tampilkan pesan success
            Toast.makeText(
                context,
                "QR Code sedang diunduh. Lihat notifikasi untuk detail.",
                Toast.LENGTH_LONG
            ).show()

            return downloadId
        } catch (e: SecurityException) {
            // Error khusus untuk authorization
            Toast.makeText(
                context,
                "Akses ditolak: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            throw e
        } catch (e: Exception) {
            // Error umum
            Toast.makeText(
                context,
                "Gagal mengunduh QR Code: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            throw e
        }
    }

    /**
     * Validasi apakah user memiliki permission untuk download QR code
     * @param tokenPreference Token preference untuk cek role
     * @return true jika user adalah admin
     */
    fun canDownloadQrCode(tokenPreference: TokenPreference): Boolean {
        return tokenPreference.isAdmin()
    }

    /**
     * Overloaded function untuk backward compatibility
     * Sekarang memerlukan TokenPreference untuk validasi role
     */
    @Deprecated("Use version with TokenPreference parameter", ReplaceWith("downloadQrCode(context, categoryId, destinationId, destinationName, tokenPreference)"))
    fun downloadQrCode(
        context: Context,
        categoryId: Int,
        destinationId: Int,
        destinationName: String
    ): Long {
        Toast.makeText(
            context,
            "Fitur download QR Code memerlukan validasi admin.",
            Toast.LENGTH_LONG
        ).show()
        throw SecurityException("Authorization required for QR code download")
    }
}