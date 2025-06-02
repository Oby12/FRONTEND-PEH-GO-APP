package com.example.peh_goapp.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.* // <- Import penting untuk Lottie
import com.example.peh_goapp.R // Pastikan R ini merujuk ke resource project Anda
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToIntroduction: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Konfigurasi untuk animasi Lottie dari direktori res/raw
    // Ganti 'R.raw.nama_animasi_anda' dengan ID resource file JSON animasi Lottie Anda.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_splash_screen))

    // State untuk mengontrol progres animasi Lottie.
    // 'iterations' bisa diatur ke 1 jika ingin animasi hanya sekali putar,
    // atau LottieConstants.IterateForever untuk berulang tanpa henti selama splash screen tampil.
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever // Animasi akan berulang
        // iterations = 1 // Jika ingin animasi hanya diputar sekali
        // speed = 1f, // Kecepatan animasi (opsional)
        // restartOnPlay = false // Opsional
    )

    // State untuk memunculkan teks setelah animasi Lottie mulai terlihat, memberikan efek yang lebih halus.
    var showTextElements by remember { mutableStateOf(false) }

    // LaunchedEffect untuk memicu logika saat composable pertama kali muncul.
    LaunchedEffect(Unit) {
        // Memberi sedikit jeda sebelum menampilkan elemen teks, agar fokus ke animasi Lottie terlebih dahulu.
        delay(500) // Sesuaikan durasi ini jika perlu
        showTextElements = true

        // Total durasi tampilan splash screen sebelum pengecekan navigasi.
        // Sesuaikan durasi 3000 milidetik (3 detik) ini sesuai kebutuhan Anda.
        // Jika animasi Lottie Anda memiliki durasi spesifik dan hanya ingin diputar sekali,
        // Anda bisa menyesuaikan delay ini agar cocok atau menggunakan callback dari Lottie.
        delay(3000)
        viewModel.checkNavigationDestination()
    }

    // LaunchedEffect untuk menangani navigasi berdasarkan perubahan state dari ViewModel.
    LaunchedEffect(uiState.navigationState) {
        when (uiState.navigationState) {
            NavigationState.TO_INTRODUCTION -> onNavigateToIntroduction()
            NavigationState.TO_LOGIN -> onNavigateToLogin()
            NavigationState.TO_HOME -> onNavigateToHome()
            NavigationState.LOADING -> { /* Tetap berada di splash screen, tidak melakukan navigasi */ }
        }
    }

    // Kontainer utama untuk splash screen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Latar belakang putih bersih.
        contentAlignment = Alignment.Center // Konten ditengah-tengahkan.
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize() // Mengisi seluruh ukuran yang tersedia.
        ) {
            // Komponen LottieAnimation untuk menampilkan animasi.
            LottieAnimation(
                composition = composition,
                progress = { progress }, // Mengikat progres animasi Lottie.
                modifier = Modifier.size(250.dp) // Sesuaikan ukuran animasi Lottie sesuai desain Anda.
            )

            Spacer(modifier = Modifier.height(32.dp)) // Jarak antara animasi dan teks.

            // Menampilkan nama aplikasi jika showTextElements adalah true.
//            if (showTextElements) {
//                Text(
//                    text = "PEH-GO",
//                    fontSize = 36.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF4DB6AC), // Warna teal yang khas.
//                    textAlign = TextAlign.Center,
//                    letterSpacing = 2.sp
//                )
//            }
        }
    }
}