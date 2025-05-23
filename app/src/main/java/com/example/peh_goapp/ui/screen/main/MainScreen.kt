package com.example.peh_goapp.ui.screen.main

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.R
import com.example.peh_goapp.ui.components.BannerSlider
import com.example.peh_goapp.ui.components.CategoryGrid
import com.example.peh_goapp.ui.components.DrawerContent
import com.example.peh_goapp.ui.components.ScannerButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onScannerClick: () -> Unit,
    onNavigateToInformation: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Dialog konfirmasi untuk logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Tambahkan state untuk menyimpan rute yang akan diakses setelah drawer ditutup
    var pendingNavigation by remember { mutableStateOf<String?>(null) }

    // Efek untuk menangani navigasi saat logout berhasil
    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            // Reset state dan navigasi ke layar login
            viewModel.clearLogoutSuccess()
            onLogout()
        }
    }
    // Effect untuk memproses navigasi setelah drawer tertutup
    LaunchedEffect(drawerState.isClosed, pendingNavigation) {
        if (drawerState.isClosed && pendingNavigation != null) {
            Log.d("MainScreen", "Drawer tertutup, navigasi ke ${pendingNavigation}")

            when (pendingNavigation) {
                "information" -> {
                    onNavigateToInformation()
                    Log.d("MainScreen", "Memanggil onNavigateToInformation()")
                }
            }

            // Reset navigasi tertunda
            pendingNavigation = null
        }
    }

    // Dialog konfirmasi logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                userName = uiState.userName,
                onNavigate = { route ->
                    pendingNavigation = route
                    scope.launch {
                        drawerState.close()
//                        delay(300)
//
//                        when (route){
//                            "information" -> onNavigateToInformation()
//                        }
                    }
                    // Handle navigation
                },
                onLogout = {
                    // Tutup drawer dan tampilkan dialog konfirmasi
                    scope.launch {
                        drawerState.close()
                        // Tambahkan delay sedikit agar drawer selesai ditutup sebelum dialog muncul
                        kotlinx.coroutines.delay(300)
                        showLogoutDialog = true
                    }
                },
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
                        .height(200.dp),
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
                        tint = Color.White
                    )
                }

                // Tambahkan tombol favorit di pojok kanan atas
                IconButton(
                    onClick = onNavigateToFavorites,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorit Saya",
                        tint = Color.Red
                    )
                }

                // Main content with rounded corners
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 160.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 50.dp) // Space for scanner button
                    ) {
                        // Banner slider
                        BannerSlider(
                            banners = uiState.banners,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        // Title for category section
                        Text(
                            text = "Recomended For You",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            modifier = Modifier
                                .padding(horizontal = 35.dp, vertical = 12.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Category grid
                        CategoryGrid(
                            categories = uiState.categories,
                            onCategoryClick = { category ->
                                onCategoryClick(category.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
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

                // Loading indicator saat proses logout
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }

    // PERBAIKAN: Error dialog hanya ditampilkan jika shouldShowError = true
    if (uiState.errorMessage != null && uiState.shouldShowError) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Peringatan") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}