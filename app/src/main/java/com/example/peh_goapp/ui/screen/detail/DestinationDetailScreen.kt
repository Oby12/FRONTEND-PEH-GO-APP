package com.example.peh_goapp.ui.screen.destinationdetail

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.R
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.example.peh_goapp.data.remote.api.ApiConfig
import com.example.peh_goapp.utils.DestinationUtils

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

    Scaffold(
        bottomBar = {
            // Bottom bar with Let's Go button and Download QR button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp) // Increased height to accommodate shadow
                    .padding(bottom = 8.dp) // Extra space at bottom
            ) {
                // Multiple shadow layers for upward shadow effect
                // Shadow layer 3 (farthest upward)
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

                // Shadow layer 2 (middle upward)
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

                // Shadow layer 1 (closest upward)
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

                // Actual bottom bar content - positioned below shadow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Let's Go button (reduced width)
                    Button(
                        onClick = {
                            try {
                                val uri = Uri.parse(uiState.urlLocation)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                viewModel.setErrorMessage("Gagal membuka lokasi: ${e.message}")
                            }
                        },
                        modifier = Modifier
                            .weight(0.7f)
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

                    // Download QR Code button (BARU)
                    // Tombol Download QR Code (Diupdate)
                    Button(
                        onClick = {
                            try {
                                // Gunakan utility class untuk download
                                DestinationUtils.downloadQrCode(
                                    context = context,
                                    categoryId = categoryId,
                                    destinationId = destinationId,
                                    destinationName = uiState.name
                                )
                            } catch (e: Exception) {
                                viewModel.setErrorMessage("Gagal mendownload QR code: ${e.message}")
                            }
                        },
                        modifier = Modifier
                            .weight(0.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF03A9F4) // Biru untuk membedakan
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download),
                            contentDescription = "Download QR Code",
                            tint = Color.White
                        )
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow
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
                            text = "Desciption",
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

                        // Pictures Header
                        Text(
                            text = "Picture",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        // Horizontally scrollable image gallery - ENLARGED PHOTOS WITH BETTER SHADOW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)  // Increased height
                                .horizontalScroll(horizontalScrollState)
                                .padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)  // More space between items
                        ) {
                            if (uiState.pictures.isNotEmpty()) {
                                uiState.pictures.forEach { picture ->
                                    val bitmap = uiState.pictureImages[picture.id]

                                    // Box to create multi-layered shadow effect for blur
                                    Box(
                                        modifier = Modifier
                                            .width(280.dp)  // Much wider image
                                            .height(150.dp)
                                            .padding(end = 2.dp, bottom = 2.dp)
                                    ) {
                                        // Multiple shadow layers with different offsets and alpha for blur effect
                                        // Shadow layer 3 (farthest)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset(x = 4.dp, y = 4.dp)
                                                .background(Color.Black.copy(alpha = 0.05f), RectangleShape)
                                        )

                                        // Shadow layer 2 (middle)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset(x = 3.dp, y = 3.dp)
                                                .background(Color.Black.copy(alpha = 0.08f), RectangleShape)
                                        )

                                        // Shadow layer 1 (closest)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset(x = 2.dp, y = 2.dp)
                                                .background(Color.Black.copy(alpha = 0.1f), RectangleShape)
                                        )

                                        // Content layer
                                        Card(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RectangleShape, // No rounded corners
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFE8F5E9) // Light green background
                                            ),
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
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFFE8F5E9)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Fallback gallery images
                                repeat(2) {
                                    // Box to create multi-layered shadow effect for blur
                                    Box(
                                        modifier = Modifier
                                            .width(280.dp)  // Much wider image
                                            .height(150.dp)
                                            .padding(end = 2.dp, bottom = 2.dp)
                                    ) {
                                        // Multiple shadow layers with different offsets and alpha for blur effect
                                        // Shadow layer 3 (farthest)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset(x = 4.dp, y = 4.dp)
                                                .background(Color.Black.copy(alpha = 0.05f), RectangleShape)
                                        )

                                        // Shadow layer 2 (middle)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset(x = 3.dp, y = 3.dp)
                                                .background(Color.Black.copy(alpha = 0.08f), RectangleShape)
                                        )

                                        // Shadow layer 1 (closest)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset(x = 2.dp, y = 2.dp)
                                                .background(Color.Black.copy(alpha = 0.1f), RectangleShape)
                                        )

                                        // Content layer
                                        Card(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RectangleShape, // No rounded corners
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFE8F5E9) // Light green background
                                            ),
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

                        // Add extra space at the bottom to avoid content being hidden behind the bottom bar
                        Spacer(modifier = Modifier.height(30.dp)) // Extra space for bottom bar
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

            // Custom top navigation bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) {
                // Back button
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

                // Title in center
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

                // Admin buttons (edit and delete)
                if (uiState.isAdmin) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Edit button
                        IconButton(
                            onClick = { onEditClick(categoryId, destinationId) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_icon),
                                contentDescription = "Edit",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { showDeleteDialog.value = true },
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

    // Delete Confirmation Dialog
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Apakah Anda yakin ingin menghapus destinasi ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                        viewModel.deleteDestination(onNavigateBack)
                    }
                ) {
                    Text("Ya, Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Error Dialog
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}