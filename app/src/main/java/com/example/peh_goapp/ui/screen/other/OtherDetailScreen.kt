package com.example.peh_goapp.ui.screen.other

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherDetailScreen(
    otherId: Int,
    onNavigateBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: OtherDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(otherId) {
        viewModel.loadOtherDetail(otherId)
    }

    LaunchedEffect(uiState.isDeleteSuccess) {
        if (uiState.isDeleteSuccess) {
            onDeleteSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Other") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.isAdmin()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // FIX: Create local variables to avoid smart cast issues
            val currentErrorMessage = uiState.errorMessage
            val currentOtherDetail = uiState.otherDetail

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                currentErrorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentErrorMessage, // Use local variable
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadOtherDetail(otherId) }) {
                            Text("Coba Lagi")
                        }
                    }
                }

                currentOtherDetail != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Cover Image
                        AsyncImage(
                            model = currentOtherDetail.coverUrl,
                            contentDescription = currentOtherDetail.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Name - FIX: Handle nullable strings properly
                        OutlinedTextField(
                            value = currentOtherDetail.name ?: "",
                            onValueChange = { },
                            label = { Text("Nama") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )

                        // Category - FIX: Handle nullable strings properly
                        OutlinedTextField(
                            value = currentOtherDetail.category ?: "",
                            onValueChange = { },
                            label = { Text("Kategori") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )

                        // Story - FIX: Handle nullable strings properly
                        OutlinedTextField(
                            value = currentOtherDetail.story ?: "",
                            onValueChange = { },
                            label = { Text("Story") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5,
                            readOnly = true
                        )

                        // Download Barcode Button (Only for Admin)
                        if (viewModel.isAdmin()) {
                            Button(
                                onClick = { viewModel.downloadBarcode(otherId) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("Download Barcode")
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada data untuk ditampilkan")
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Other") },
            text = { Text("Apakah Anda yakin ingin menghapus item Other ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteOther(otherId)
                    }
                ) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}