package com.example.peh_goapp.ui.screen.destinationdetail

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.BuildConfig
import com.example.peh_goapp.R
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.example.peh_goapp.data.remote.api.ApiConfig
import com.example.peh_goapp.utils.DestinationUtils
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.content.ActivityNotFoundException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationDetailScreen(
    categoryId: Int,
    destinationId: Int,
    onNavigateBack: () -> Unit,
    onEditClick: (Int, Int) -> Unit,
    tokenPreference: TokenPreference,
    base64ImageService: Base64ImageService,
    viewModel: DestinationDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val showDeleteDialog = remember { mutableStateOf(false) }

    // Load destination detail when screen is composed
    LaunchedEffect(categoryId, destinationId) {
        viewModel.loadDestinationDetail(categoryId, destinationId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                // Dynamic Bottom bar based on user role
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .padding(bottom = 8.dp)
                ) {
                    // Multiple shadow layers for upward shadow effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 4.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.03f),
                                shape = RectangleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 2.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.05f),
                                shape = RectangleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 0.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.08f),
                                shape = RectangleShape
                            )
                    )

                    // Actual bottom bar content
                    if (uiState.isAdmin) {
                        // ADMIN LAYOUT: 2 buttons (Let's Go, Download QR)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.White)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Let's Go button
                            Button(
                                onClick = {
                                    try {
                                        if (uiState.urlLocation.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uiState.urlLocation))
                                            context.startActivity(intent)
                                        } else {
                                            viewModel.setErrorMessage("URL lokasi tidak tersedia")
                                        }
                                    } catch (e: Exception) {
                                        viewModel.setErrorMessage("Gagal membuka lokasi: ${e.message}")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Let's Go",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_navigation),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                }
                            }

                            // Download QR Code button - HANYA UNTUK ADMIN
                            Button(
                                onClick = {
                                    try {
                                        if (!viewModel.canDownloadQrCode()) {
                                            viewModel.setErrorMessage("Akses ditolak. Fitur download QR Code hanya untuk admin.")
                                            return@Button
                                        }
                                        DestinationUtils.downloadQrCode(
                                            context = context,
                                            categoryId = categoryId,
                                            destinationId = destinationId,
                                            destinationName = uiState.name,
                                            tokenPreference = tokenPreference
                                        )
                                    } catch (e: SecurityException) {
                                        viewModel.setErrorMessage("Akses ditolak: ${e.message}")
                                    } catch (e: Exception) {
                                        viewModel.setErrorMessage("Gagal mendownload QR code: ${e.message}")
                                    }
                                },
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF03A9F4)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_download),
                                        contentDescription = "Download QR Code",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "QR",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // WISATAWAN LAYOUT: hanya 1 button (Let's Go) di tengah
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.White)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        if (uiState.urlLocation.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uiState.urlLocation))
                                            context.startActivity(intent)
                                        } else {
                                            viewModel.setErrorMessage("URL lokasi tidak tersedia")
                                        }
                                    } catch (e: Exception) {
                                        viewModel.setErrorMessage("Gagal membuka lokasi: ${e.message}")
                                    }
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .widthIn(min = 200.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Let's Go",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_navigation),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                // Main content - Full screen with scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    // Cover Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (uiState.coverImage != null) {
                            Image(
                                bitmap = uiState.coverImage!!.asImageBitmap(),
                                contentDescription = "Cover Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.slider_satu),
                                contentDescription = "Cover Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Content Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-20).dp),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            // Destination Name
                            Text(
                                text = uiState.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                textAlign = TextAlign.Center
                            )

                            // Address with location icon
                            Row(
                                modifier = Modifier.padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.address,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            // Description Header
                            Text(
                                text = "Description",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            // Description content
                            Text(
                                text = uiState.description,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // YouTube Video Section - Tambahkan setelah Description
                            if (!uiState.youtubeUrl.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // Video Header dengan Icon YouTube
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Menggunakan icon play dari compose icons by revisi
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleOutline,
                                        contentDescription = "YouTube Video",
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Video Destinasi",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // YouTube Video Player Component
                                YouTubeVideoPlayer(
                                    videoUrl = uiState.youtubeUrl!!,
                                    embedUrl = uiState.youtubeEmbedUrl
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Pictures Header
                            Text(
                                text = "Picture",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            // Horizontally scrollable image gallery
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .horizontalScroll(horizontalScrollState)
                                    .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (uiState.pictures.isNotEmpty()) {
                                    uiState.pictures.forEach { picture ->
                                        val bitmap = uiState.pictureImages[picture.id]
                                        Box(
                                            modifier = Modifier
                                                .width(280.dp)
                                                .height(150.dp)
                                                .padding(end = 2.dp, bottom = 2.dp)
                                        ) {
                                            // Shadow layers
                                            Box(modifier = Modifier.fillMaxSize().offset(x = 4.dp, y = 4.dp).background(Color.Black.copy(alpha = 0.05f), RectangleShape))
                                            Box(modifier = Modifier.fillMaxSize().offset(x = 3.dp, y = 3.dp).background(Color.Black.copy(alpha = 0.08f), RectangleShape))
                                            Box(modifier = Modifier.fillMaxSize().offset(x = 2.dp, y = 2.dp).background(Color.Black.copy(alpha = 0.1f), RectangleShape))
                                            Card(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RectangleShape,
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                            ) {
                                                if (bitmap != null) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = "Gallery Image",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Default placeholder images
                                    repeat(2) {
                                        Box(
                                            modifier = Modifier
                                                .width(280.dp)
                                                .height(150.dp)
                                                .padding(end = 2.dp, bottom = 2.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize().offset(x = 4.dp, y = 4.dp).background(Color.Black.copy(alpha = 0.05f), RectangleShape))
                                            Box(modifier = Modifier.fillMaxSize().offset(x = 3.dp, y = 3.dp).background(Color.Black.copy(alpha = 0.08f), RectangleShape))
                                            Box(modifier = Modifier.fillMaxSize().offset(x = 2.dp, y = 2.dp).background(Color.Black.copy(alpha = 0.1f), RectangleShape))
                                            Card(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RectangleShape,
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.slider_satu),
                                                    contentDescription = "Gallery Image",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                // Loading indicator
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }

                // Custom top navigation bar with conditional admin buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Detail",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    if (uiState.isAdmin) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (viewModel.validateAdminPermission("edit destinasi")) {
                                        onEditClick(categoryId, destinationId)
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit_icon),
                                    contentDescription = "Edit Destinasi",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (viewModel.validateAdminPermission("hapus destinasi")) {
                                        showDeleteDialog.value = true
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete_icon),
                                    contentDescription = "Delete",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button for Favorite
        FloatingActionButton(
            onClick = { viewModel.toggleFavorite() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
                .zIndex(10f),
            containerColor = if (uiState.isFavorite) Color(0xFF4CAF50) else Color.White,
            contentColor = if (uiState.isFavorite) Color.White else Color(0xFF4CAF50)
        ) {
            Icon(
                imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (uiState.isFavorite) "Hapus dari favorit" else "Tambahkan ke favorit"
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog.value && uiState.isAdmin) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Apakah Anda yakin ingin menghapus destinasi \"${uiState.name}\"? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                        viewModel.deleteDestination(onNavigateBack)
                    }
                ) {
                    Text("Ya, Hapus", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Error dialog
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = {
                Text(
                    text = if (uiState.errorMessage!!.contains("Akses ditolak") ||
                        uiState.errorMessage!!.contains("Unauthorized")) {
                        "Akses Ditolak"
                    } else {
                        "Error"
                    }
                )
            },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}


// Komponen YouTube Video Player
// Komponen YouTube Video Player yang diperbaiki
// Letakkan di bagian bawah file DestinationDetailScreen.kt

// Komponen YouTube Video Player yang diperbaiki
@Composable
fun YouTubeVideoPlayer(
    videoUrl: String,
    embedUrl: String?
) {
    val context = LocalContext.current
    var showWebView by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        if (showWebView && embedUrl != null) {
            // WebView untuk embed YouTube
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            allowContentAccess = false
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_NO_CACHE
                        }
                        webViewClient = WebViewClient()
                        webChromeClient = WebChromeClient()

                        val html = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="utf-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <style>
                                    * { margin: 0; padding: 0; }
                                    body { background: black; }
                                    #player-container {
                                        position: relative;
                                        width: 100%;
                                        height: 100vh;
                                        overflow: hidden;
                                    }
                                    iframe {
                                        position: absolute;
                                        top: 0;
                                        left: 0;
                                        width: 100%;
                                        height: 100%;
                                    }
                                </style>
                            </head>
                            <body>
                                <div id="player-container">
                                    <iframe 
                                        id="youtube-player"
                                        src="$embedUrl?autoplay=0&rel=0&modestbranding=1&playsinline=1"
                                        frameborder="0" 
                                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                        allowfullscreen>
                                    </iframe>
                                </div>
                            </body>
                            </html>
                        """.trimIndent()

                        loadDataWithBaseURL(
                            "https://www.youtube.com",
                            html,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Thumbnail dengan tombol play
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (embedUrl != null) {
                            showWebView = true
                        } else {
                            // Buka di YouTube app atau browser
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                intent.setPackage("com.google.android.youtube")
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                context.startActivity(intent)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1a1a1a))
                )

                // Play button
                Card(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Text instruksi
                Text(
                    text = "Ketuk untuk memutar video",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}