package com.example.peh_goapp.ui.screen.scanresult

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.R

@Composable
fun ScanResultScreen(
    categoryId: Int,
    destinationId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ScanResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val verticalScrollState = rememberScrollState()

    // Memuat data destinasi saat layar dibuka
    LaunchedEffect(categoryId, destinationId) {
        viewModel.loadDestinationDetail(categoryId, destinationId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Back button di pojok kiri atas
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp)
                .align(Alignment.TopStart)
                .zIndex(2f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.Black
            )
        }

        // Title "Result" di tengah atas
        Text(
            text = "Result",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(top = 24.dp)
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )

        if (uiState.isLoading) {
            // Loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
        } else {
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp) // Beri ruang untuk judul di atas
                    .verticalScroll(verticalScrollState)
                    .padding(bottom = 16.dp)
            ) {
                // Cover image sebagai card dengan rounded corners dan padding
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(180.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = Color.Black.copy(alpha = 0.3f),
                            ambientColor = Color.Black.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                }

                // Main content with cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Nama Destinasi
                    Text(
                        text = "Nama Destinasi",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(24.dp), // Sangat melengkung
                                spotColor = Color.Black.copy(alpha = 0.2f),
                                ambientColor = Color.Black.copy(alpha = 0.1f)
                            ),
                        shape = RoundedCornerShape(24.dp), // Sangat melengkung
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp), // Gunakan shadow di modifier
                        border = BorderStroke(0.5.dp, Color.LightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = uiState.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }

                    // Alamat Destinasi
                    Text(
                        text = "Alamat Destinasi",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color.Black.copy(alpha = 0.2f),
                                ambientColor = Color.Black.copy(alpha = 0.1f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(0.5.dp, Color.LightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = uiState.address,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }

                    // Deskripsi
                    Text(
                        text = "Deskripsi",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color.Black.copy(alpha = 0.2f),
                                ambientColor = Color.Black.copy(alpha = 0.1f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(0.5.dp, Color.LightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = uiState.description,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }

                    // Pictures gallery
                    if (uiState.pictureImages.isNotEmpty()) {
                        Text(
                            text = "Galeri",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                        )

                        PictureGalleryImproved(
                            images = uiState.pictureImages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 8.dp)
                        )
                    }

                    // Status kunjungan
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Text(
                            text = "Selamat!\nAnda telah mengunjungi destinasi ini",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }

        // Error dialog
        if (uiState.errorMessage != null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Error") },
                text = { Text(uiState.errorMessage!!) },
                confirmButton = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Kembali")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PictureGalleryImproved(
    images: List<Bitmap>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState { images.size }

    Box(modifier = modifier) {
        // Main image pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 4.dp,
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) { page ->
            Image(
                bitmap = images[page].asImageBitmap(),
                contentDescription = "Gallery Image ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Pager indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(images.size) { index ->
                val color = if (pagerState.currentPage == index) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}