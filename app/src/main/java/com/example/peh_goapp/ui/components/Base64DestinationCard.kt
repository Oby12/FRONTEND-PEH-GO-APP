package com.example.peh_goapp.ui.components

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peh_goapp.R
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.DestinationModel
import com.example.peh_goapp.data.remote.api.Base64ImageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Composable untuk menampilkan card destinasi dengan gambar Base64
 * Solusi untuk masalah "Failed to create image decoder with message 'unimplemented'"
 */
@Composable
fun Base64DestinationCard(
    destination: DestinationModel,
    tokenPreference: TokenPreference,
    base64ImageService: Base64ImageService,
    onClick: () -> Unit
) {
    // State untuk bitmap
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }
    val maxRetries = 2

    // Load bitmap dengan retry logic
    LaunchedEffect(destination.id, retryCount) {
        if (retryCount <= maxRetries) {
            try {
                isLoading = true
                hasError = false

                Log.d("Base64DestinationCard", "Loading image for destination ${destination.id} (attempt ${retryCount + 1})")

                // Tambahkan delay sebelum retry untuk menghindari request berturut-turut yang terlalu cepat
                if (retryCount > 0) {
                    withContext(Dispatchers.IO) {
                        delay(1000 * retryCount.toLong())
                    }
                }

                val result = withContext(Dispatchers.IO) {
                    base64ImageService.getDestinationCoverImage(destination.id)
                }

                bitmap = result

                if (result == null) {
                    Log.e("Base64DestinationCard", "Failed to load image for destination ${destination.id}")
                    hasError = true
                    // Coba retry jika masih dalam batas maksimum
                    if (retryCount < maxRetries) {
                        retryCount++
                    }
                } else {
                    Log.d("Base64DestinationCard", "Successfully loaded image for destination ${destination.id}")
                }
            } catch (e: Exception) {
                Log.e("Base64DestinationCard", "Error loading image: ${e.message}", e)
                hasError = true
                // Coba retry jika masih dalam batas maksimum
                if (retryCount < maxRetries) {
                    retryCount++
                }
            } finally {
                isLoading = false
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image content
            when {
                isLoading -> {
                    // Show loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF4CAF50)
                    )
                }
                bitmap != null -> {
                    // Show bitmap image
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = destination.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    // Show placeholder
                    Image(
                        painter = painterResource(id = R.drawable.slider_satu),
                        contentDescription = destination.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Gradient overlay untuk meningkatkan keterbacaan teks
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Text content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Destination name
                Text(
                    text = destination.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Destination address
                Text(
                    text = destination.address,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}