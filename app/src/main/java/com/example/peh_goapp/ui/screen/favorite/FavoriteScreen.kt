package com.example.peh_goapp.ui.screen.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.example.peh_goapp.ui.components.Base64DestinationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onNavigateBack: () -> Unit,
    onDestinationClick: (Int) -> Unit,
    tokenPreference: TokenPreference,
    base64ImageService: Base64ImageService,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorit Saya") },
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
                    placeholder = { Text("Cari Favorit", color = Color.Gray) },
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
                                .background(Color(0xFF4CAF50), RoundedCornerShape(20.dp))
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

            // Favorite list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else if (uiState.filteredFavorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty())
                            "Tidak ada favorit yang sesuai dengan pencarian"
                        else
                            "Belum ada destinasi favorit",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.filteredFavorites,
                        key = { it.id } // Gunakan ID sebagai key untuk stabilitas item
                    ) { destination ->
                        // Gunakan key untuk item composition untuk mencegah masalah rekomposisi
                        key(destination.id) {
                            // Menggunakan Base64DestinationCard dari komponen yang sudah ada
                            Base64DestinationCard(
                                destination = destination,
                                tokenPreference = tokenPreference,
                                base64ImageService = base64ImageService,
                                onClick = { onDestinationClick(destination.id) }
                            )
                        }
                    }
                    // Extra space at bottom
                    item { Spacer(modifier = Modifier.height(16.dp)) }
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
                    TextButton(onClick = { viewModel.loadFavorites() }) {
                        Text("Coba Lagi")
                    }
                }
            }
        )
    }
}