package com.example.peh_goapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.peh_goapp.data.model.BannerModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerSlider(
    banners: List<BannerModel>,
    autoSlideDuration: Long = 3000, // 3 detik
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-slide
    LaunchedEffect(pagerState) {
        while (true) {
            delay(autoSlideDuration)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Banner content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // Mengurangi height dari 170dp ke 150dp untuk banner lebih proporsional
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                BannerItem(banner = banners[page])
            }
        }

        // Indikator di luar banner
        Spacer(modifier = Modifier.height(4.dp)) // Mengurangi jarak dari 6dp ke 4dp

        // Indikator titik di luar banner
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val color = if (isSelected) Color(0xFF4CAF50) else Color.LightGray

                Box(
                    modifier = Modifier
                        .size(6.dp) // Mengurangi ukuran dari 8dp ke 6dp
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun BannerItem(banner: BannerModel) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(10.dp), // Mengurangi radius dari 12dp ke 10dp
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Mengurangi elevasi dari 2dp ke 1dp
    ) {
        Image(
            painter = painterResource(id = banner.imageUrl),
            contentDescription = banner.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}