package com.example.peh_goapp.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.peh_goapp.R
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.model.DestinationModel
import com.example.peh_goapp.data.remote.api.ApiConfig

/**
 * Composable untuk menampilkan card destinasi wisata
 * Menampilkan gambar cover sebagai background dengan nama dan alamat di atasnya
 */
@Composable
fun DestinationCard(
    destination: DestinationModel,
    tokenPreference: TokenPreference,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val placeholderImage = R.drawable.slider_satu

    // Track error state
    var hasError by remember { mutableStateOf(false) }

    // Generate URL untuk gambar cover
    val imageUrl = if (!hasError) {
        try {
            ApiConfig.getCoverImageUrl(destination.id)
        } catch (e: Exception) {
            Log.e("DestinationCard", "Error creating URL: ${e.message}")
            hasError = true
            ""
        }
    } else ""

    // Log untuk debugging
    Log.d("DestinationCard", "Loading image for ${destination.name} (ID: ${destination.id})")
    Log.d("DestinationCard", "URL: $imageUrl")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // Full-width image as background
        Box(modifier = Modifier.fillMaxSize()) {
            // Gambar (placeholder atau dari URL)
            val painter = if (hasError) {
                // Jika sudah ada error, langsung gunakan placeholder
                painterResource(id = placeholderImage)
            } else {
                // Coba load gambar dari URL
                rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .error(placeholderImage)
                        .placeholder(placeholderImage)
                        .listener(
                            onError = { _, error ->
                                Log.e("DestinationCard", "Image load error: ${error.throwable.message}")
                                hasError = true
                            }
                        )
                        .build()
                )
            }

            // Monitor state untuk debugging
            if (!hasError && painter is AsyncImagePainter) {
                when (val state = painter.state) {
                    is AsyncImagePainter.State.Success -> {
                        Log.d("DestinationCard", "Image loaded successfully: $imageUrl")
                    }
                    is AsyncImagePainter.State.Error -> {
                        val errorMsg = state.result.throwable.message ?: "Unknown error"
                        Log.e("DestinationCard", "Error loading image: $errorMsg")
                        hasError = true
                    }
                    else -> {
                        // Loading state - menggunakan placeholder
                    }
                }
            }

            // Image component
            Image(
                painter = painter,
                contentDescription = destination.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

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