package com.example.peh_goapp.ui.screen.introduction

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
// Pastikan PagerState diimpor jika belum ada
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.R // Pastikan R ini merujuk ke resource project Anda
import kotlinx.coroutines.launch

/**
 * Mendefinisikan FontFamily untuk Poppins.
 * Pastikan file font 'poppins_regular.ttf', 'poppins_mediumitalic.ttf', dan 'poppins_semibold.ttf'
 * (atau nama yang sesuai) ada di res/font/.
 */
val poppinsFont = FontFamily(
    Font(R.font.poppins_regular),
    Font(R.font.poppins_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.poppins_semibold, FontWeight.SemiBold)
)

/**
 * Model untuk data halaman introduction
 */
data class IntroductionPage(
    val titleLine1: String? = null, // Baris pertama judul, opsional
    val titleLine2: String,         // Baris kedua judul, utama
    val description: String,
    val imageRes: Int
)

/**
 * Screen introduction yang ditampilkan pada first time install
 * Diperbarui sesuai dengan desain dan permintaan.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroductionScreen(
    onFinishIntroduction: () -> Unit,
    viewModel: IntroductionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Data halaman introduction
    val introductionPages = listOf(
        IntroductionPage(
            titleLine1 = "welcome to", // Baris pertama untuk slide 1
            titleLine2 = "PEH-GO",     // Baris kedua untuk slide 1
            description = "Temukan pesona Palembang dalam genggamanmu. Dari sejarah yang megah, kuliner menggoda, hingga destinasi tersembunyi yang menanti untuk dijelajahi! PEH-GO siap jadi teman wisatamu!", //
            imageRes = R.drawable.first_illustration_introduction // GANTI DENGAN ID DRAWABLE YANG BENAR
        ),
        IntroductionPage(
            titleLine2 = "Explore Iconic & Hidden Destinations", //
            description = "Temukan pesona Palembang dari Jembatan Ampera hingga sudut kota yang jarang diketahui. Dengan PEH-GO, setiap langkahmu adalah cerita baru.", //
            imageRes = R.drawable.second_illustration_introduction // GANTI DENGAN ID DRAWABLE YANG BENAR
        ),
        IntroductionPage(
            titleLine2 = "Ready to Explore Palembang?", //
            description = "Waktunya mulai petualangan seru bersama PEH-GO. Dapatkan panduan, rekomendasi, dan inspirasi perjalanan semua dalam genggamanmu.", //
            imageRes = R.drawable.third_illustration_introduction // GANTI DENGAN ID DRAWABLE YANG BENAR
        )
    )

    val pagerState = rememberPagerState(pageCount = { introductionPages.size }) //
    val scope = rememberCoroutineScope() //

    LaunchedEffect(uiState.isFinished) { //
        if (uiState.isFinished) { //
            onFinishIntroduction() //
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) //
    ) {
        Column(
            modifier = Modifier.fillMaxSize() //
        ) {
            // Header dengan tombol Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 16.dp), //
                horizontalArrangement = Arrangement.End, //
                verticalAlignment = Alignment.CenterVertically //
            ) {
                if (pagerState.currentPage < introductionPages.size - 1) { //
                    TextButton(
                        onClick = { //
                            viewModel.finishIntroduction() //
                        },
                        modifier = Modifier.heightIn(min = 48.dp) //
                    ) {
                        Text(
                            text = "Skip", //
                            color = Color.Gray, //
                            fontSize = 16.sp, //
                            fontWeight = FontWeight.Medium, //
                            fontFamily = poppinsFont //
                        )
                    }
                } else {
                    Spacer(Modifier.width(60.dp).heightIn(min = 48.dp)) //
                }
            }

            // Pager untuk konten halaman
            HorizontalPager(
                state = pagerState, //
                modifier = Modifier.weight(1f) // Memberi sisa ruang ke pager
            ) { pageIndex -> // Mengganti 'page' menjadi 'pageIndex' untuk kejelasan
                IntroductionPageContent(
                    pageData = introductionPages[pageIndex], //
                    isFirstPage = pageIndex == 0, //
                    currentPage = pagerState.currentPage, // Meneruskan currentPage
                    pageCount = introductionPages.size,   // Meneruskan pageCount
                    modifier = Modifier.fillMaxSize() //
                )
            }

            // Bagian bawah: Hanya tombol Next/Start di kanan bawah
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp), // Padding untuk tombol
                horizontalArrangement = Arrangement.End, // Tombol ke kanan
                verticalAlignment = Alignment.CenterVertically //
            ) {
                Button(
                    onClick = { //
                        if (pagerState.currentPage < introductionPages.size - 1) { //
                            scope.launch { //
                                pagerState.animateScrollToPage(pagerState.currentPage + 1) //
                            }
                        } else {
                            viewModel.finishIntroduction() //
                        }
                    },
                    modifier = Modifier.height(45.dp), // Ukuran tombol yang Anda set sebelumnya
                    shape = RoundedCornerShape(13.dp), // Bentuk tombol yang Anda set sebelumnya
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF45C147) // Warna tombol
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage < introductionPages.size - 1) "NEXT" else "START", //
                        fontWeight = FontWeight.Bold, //
                        fontFamily = poppinsFont, //
                        color = Color.White, //
                        fontSize = 18.sp //
                    )
                }
            }
        }
    }
}

/**
 * Konten untuk setiap halaman introduction.
 */
@Composable
fun IntroductionPageContent(
    pageData: IntroductionPage, //
    isFirstPage: Boolean, //
    currentPage: Int, // Parameter untuk halaman saat ini
    pageCount: Int,   // Parameter untuk jumlah total halaman
    modifier: Modifier = Modifier //
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp) //
            .fillMaxHeight(), //
        horizontalAlignment = Alignment.CenterHorizontally, //
        verticalArrangement = Arrangement.Top // Konten dimulai dari atas
    ) {
        // Spacer untuk memberi ruang dari atas (setelah tombol Skip)
        Spacer(modifier = Modifier.height(16.dp)) //

        // Judul dengan AnnotatedString
        Text(
            text = buildAnnotatedString { //
                val titleLine1Style = SpanStyle( //
                    color = Color.Black, //
                    fontSize = 26.sp, //
                    fontFamily = poppinsFont, //
                    fontStyle = FontStyle.Italic, //
                    fontWeight = FontWeight.Normal //
                )
                val pehGoTitleStyle = SpanStyle( //
                    color = Color(0xFF4CAF50), //
                    fontSize = 32.sp, //
                    fontFamily = poppinsFont, //
                    fontWeight = FontWeight.Bold //
                )

                if (isFirstPage && pageData.titleLine1 != null) { //
                    withStyle(style = titleLine1Style) { append(pageData.titleLine1) } //
                    append("\n") //
                    withStyle(style = pehGoTitleStyle) { append(pageData.titleLine2) } //
                } else {
                    withStyle(style = titleLine1Style) { append(pageData.titleLine2) } //
                }
            },
            textAlign = TextAlign.Center, //
            lineHeight = if (isFirstPage && pageData.titleLine1 != null) 38.sp else 32.sp, //
            modifier = Modifier.padding(bottom = 24.dp) //
        )

        // Gambar Ilustrasi (mengambil ruang yang fleksibel)
        Box(
            modifier = Modifier
                .weight(1f) // Ini akan mendorong deskripsi dan indikator ke bawah jika perlu
                .fillMaxWidth() //
                .padding(horizontal = 16.dp), //
            contentAlignment = Alignment.Center //
        ) {
            Image(
                painter = painterResource(id = pageData.imageRes), //
                contentDescription = "${pageData.titleLine1 ?: ""} ${pageData.titleLine2}", //
                modifier = Modifier
                    .fillMaxWidth() //
                    .aspectRatio(1f), // Menjaga rasio aspek, sesuaikan jika perlu
                contentScale = ContentScale.Fit //
            )
        }

        // Deskripsi
        Text(
            text = pageData.description, //
            fontFamily = poppinsFont, //
            fontStyle = FontStyle.Normal, //
            fontSize = 15.sp, //
            textAlign = TextAlign.Center, //
            color = Color.DarkGray, //
            lineHeight = 22.sp, //
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp) // Padding atas dan bawah deskripsi
        )

        // Indikator halaman (dots) tepat di bawah deskripsi
        Row(
            modifier = Modifier.padding(bottom = 32.dp), // Padding bawah untuk indikator, sebelum area tombol
            horizontalArrangement = Arrangement.spacedBy(10.dp) //
        ) {
            repeat(pageCount) { index -> //
                val isSelected = currentPage == index //
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 10.dp else 8.dp) //
                        .clip(CircleShape) //
                        .background(
                            if (isSelected) Color(0xFF45C147) // Warna hijau tombol
                            else Color.LightGray //
                        )
                )
            }
        }
    }
}