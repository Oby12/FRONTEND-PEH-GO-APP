package com.example.peh_goapp.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peh_goapp.R

private const val TAG = "DrawerContent"

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val contentDescription: String
)

@Composable
fun DrawerContent(
    userName: String,
    isAdmin: Boolean, // Parameter baru untuk mengecek role admin
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Debugging log untuk memastikan userName dan isAdmin diterima dengan benar
    Log.d(TAG, "DrawerContent dirender dengan userName: '$userName', isAdmin: $isAdmin")

    Box(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // Header with background image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.navigation_drawer_ilustration),
                    contentDescription = "Header Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // App name
                Text(
                    text = "TOUR APP",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            // Greeting section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Tampilkan nama pengguna, gunakan nilai fallback jika kosong
                    val displayName = if (userName.isNotBlank()) userName else "Pengguna"

                    Text(
                        text = "Halo!! $displayName",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        style = TextStyle(lineHeight = 24.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Have A Great Day",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu items
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Logout option with icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Log.d(TAG, "Tombol logout diklik")
                            onLogout()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "LOGOUT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.LightGray
                )

                // Information option with icon - HANYA TAMPIL UNTUK ADMIN
                if (isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Log.d(TAG, "Tombol information diklik (Admin)")
                                onNavigate("information")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "INFORMATION",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // Log untuk debugging bahwa menu information tidak ditampilkan untuk non-admin
                    Log.d(TAG, "Menu Information tidak ditampilkan - User bukan admin")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom decoration (bridge illustration)
            Image(
                painter = painterResource(id = R.drawable.logo_chamring),
                contentDescription = "Bottom Decoration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}