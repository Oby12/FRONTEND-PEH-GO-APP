package com.example.peh_goapp.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.R
import com.example.peh_goapp.ui.components.BannerSlider
import com.example.peh_goapp.ui.components.CategoryGrid
import com.example.peh_goapp.ui.components.DrawerContent
import com.example.peh_goapp.ui.components.ScannerButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onScannerClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavigate = { route ->
                    // Handle navigation
                },
                onLogout = onLogout,
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Background image (bridge)
                Image(
                    painter = painterResource(id = R.drawable.ampera_bridge),
                    contentDescription = "Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), // Mengembalikan ke 200dp agar gambar jembatan lebih terlihat
                    contentScale = ContentScale.FillWidth
                )

                // Ikon navigation drawer
                IconButton(
                    onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Menu",
                        tint = Color.White // Mengubah warna ikon menu menjadi putih agar lebih terlihat
                    )
                }

                // Main content with rounded corners
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 160.dp) // Menaikkan nilai padding dari 140dp ke 160dp agar lebih rendah
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 50.dp) // Space for scanner button
                    ) {
                        // Menghapus teks "Wisata Gunung Indahnya Alam Indonesia"

                        // Banner slider - langsung ditampilkan tanpa judul
                        BannerSlider(
                            banners = uiState.banners,
                            modifier = Modifier.padding(top = 16.dp) // Mengurangi padding atas
                        )

                        // Title for category section
                        Text(
                            text = "Recomended For You",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp), // Mengurangi ukuran font
                            modifier = Modifier
                                .padding(horizontal = 35.dp, vertical = 12.dp)
                                .align(Alignment.CenterHorizontally)// Mengurangi padding
                        )

                        // Category grid dengan height yang disesuaikan
                        CategoryGrid(
                            categories = uiState.categories,
                            onCategoryClick = { category ->
                                onCategoryClick(category.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp) // Mengurangi height lagi dari 180dp ke 160dp
                        )
                    }
                }

                // Scanner button positioned at the bottom
                ScannerButton(
                    onClick = onScannerClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
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
            }
        )
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(
        onLogout = {},
        onCategoryClick = {},
        onScannerClick = {}
    )
}