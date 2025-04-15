package com.example.peh_goapp.ui.screen.destination

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.example.peh_goapp.ui.components.Base64DestinationCard

/**
 * Screen untuk menampilkan daftar destinasi berdasarkan kategori
 * Menggunakan pendekatan Base64 untuk gambar untuk mengatasi masalah decoder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDestinationsScreen(
    categoryId: Int,
    onNavigateBack: () -> Unit,
    onDestinationClick: (Int) -> Unit,
    onAddDestinationClick: (Int) -> Unit,
    tokenPreference: TokenPreference,
    base64ImageService: Base64ImageService,
    viewModel: DestinationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isAdmin = remember { tokenPreference.isAdmin() }

    // Set category ID when the screen is composed
    LaunchedEffect(categoryId) {
        viewModel.setCategoryId(categoryId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.categoryName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            if (isAdmin && uiState.categoryId > 0) { // Hanya tampilkan FAB jika user ADMIN dan kategori valid
                FloatingActionButton(
                    onClick = { onAddDestinationClick(categoryId) },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Destinasi")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5)) // Light gray background
        ) {
            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("Cari Tempat", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    trailingIcon = {
                        // Green circle with magnifying glass icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                )
            }

            // Pull to refresh functionality could be added here with SwipeRefresh

            // Destination list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else if (uiState.filteredDestinations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada destinasi ditemukan",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.filteredDestinations,
                        key = { it.id } // Gunakan ID sebagai key untuk stabilitas item
                    ) { destination ->
                        // Gunakan key untuk item composition untuk mencegah masalah rekomposisi
                        key(destination.id) {
                            // Menggunakan Base64DestinationCard
                            Base64DestinationCard(
                                destination = destination,
                                tokenPreference = tokenPreference,
                                base64ImageService = base64ImageService,
                                onClick = { onDestinationClick(destination.id) }
                            )
                        }
                    }
                    // Extra space at bottom for FloatingActionButton
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Error dialog
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            },
            // Tambahkan tombol retry jika perlu
            dismissButton = {
                if (!uiState.isLoading) {
                    TextButton(onClick = { viewModel.refreshDestinations() }) {
                        Text("Coba Lagi")
                    }
                }
            }
        )
    }
}