package com.example.peh_goapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.peh_goapp.R

@Composable
fun ScannerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(70.dp), // Mengurangi ukuran dari 56dp ke 52dp
            containerColor = Color(0xFF4CAF50),
            shape = CircleShape
        ) {
            Image(
                painter = painterResource(id = R.drawable.qr_qode_scanner_ic),
                contentDescription = "QR Scanner",
                modifier = Modifier.size(26.dp) // Mengurangi ukuran dari 28dp ke 26dp
            )
        }
    }
}