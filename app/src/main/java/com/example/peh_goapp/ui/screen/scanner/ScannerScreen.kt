package com.example.peh_goapp.ui.screen.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.R
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    onScanSuccess: (Int, Int) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // Kamera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setCameraPermission(isGranted)
    }

    // Cek izin kamera saat layar dibuka
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) -> {
                viewModel.setCameraPermission(true)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Navigasi ke result screen ketika scan berhasil
    LaunchedEffect(uiState.parsedData) {
        uiState.parsedData?.let { data ->
            onScanSuccess(data.categoryId, data.destinationId)
            viewModel.resetScan()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.hasCameraPermission) {
                // Camera preview dan scanner
                Box(modifier = Modifier.fillMaxSize()) {
                    // Scanner camera
                    CameraPreview(
                        onQrCodeScanned = { result ->
                            viewModel.processScanResult(result)
                        },
                        isTorchEnabled = uiState.isTorchOn
                    )

                    // Overlay scanline
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 2.dp,
                                color = Color(0xFF4CAF50),
                                shape = RoundedCornerShape(20.dp)
                            )
                    )

                    // Instruksi
                    Text(
                        text = "Arahkan kamera ke QR Code destinasi",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 24.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Flash toggle button
                    IconButton(
                        onClick = { viewModel.toggleTorch() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .size(56.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    ) {
                        // Menggunakan resource drawable untuk menggantikan Icons.Default.FlashOn/FlashOff
                        Icon(
                            painter = painterResource(
                                id = if (uiState.isTorchOn) R.drawable.ic_flash_off
                                else R.drawable.ic_flash_on
                            ),
                            contentDescription = "Toggle Flash",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Error message
                    if (uiState.errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            )
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Permission not granted
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Izin kamera diperlukan untuk memindai QR code",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Berikan Izin Kamera")
                    }
                }
            }
        }
    }
}

/**
 * Camera preview untuk memindai QR code
 *
 * @param onQrCodeScanned Callback yang dipanggil ketika QR code berhasil dipindai
 * @param isTorchEnabled Status torch kamera (flash)
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    onQrCodeScanned: (String) -> Unit,
    isTorchEnabled: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val preview = remember { Preview.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    // Barcode scanner options
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    val barcodeScanner = remember { BarcodeScanning.getClient(options) }

    // Image analysis untuk scan barcode
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (barcodes.isNotEmpty()) {
                                    barcodes[0].rawValue?.let { rawValue ->
                                        onQrCodeScanned(rawValue)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    // Unbind all use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )

                    // Set torch mode
                    if (camera.cameraInfo.hasFlashUnit()) {
                        camera.cameraControl.enableTorch(isTorchEnabled)
                    }

                    preview.setSurfaceProvider(view.surfaceProvider)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}