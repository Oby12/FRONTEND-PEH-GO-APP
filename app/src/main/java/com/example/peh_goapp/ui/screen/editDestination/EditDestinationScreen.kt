package com.example.peh_goapp.ui.screen.editdestination

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDestinationScreen(
    categoryId: Int,
    destinationId: Int,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: EditDestinationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Set categoryId and destinationId when screen is first composed
    LaunchedEffect(categoryId, destinationId) {
        viewModel.loadDestinationDetail(categoryId, destinationId)
    }

    // Show success message when operation is successful
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Tampilkan snackbar atau toast
            Toast.makeText(
                context,
                "Destinasi berhasil diupdate!",
                Toast.LENGTH_SHORT
            ).show()

            // Delay sedikit sebelum navigasi kembali
            delay(500)
            onSuccess()
        }
    }

    // Image picker for cover image
    val coverImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setCoverImage(it) }
    }

    // Image picker for additional images
    val pictureImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPictureImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold
                    )
                },
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else {
                // Form content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Cover image selection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(Color.Gray, RoundedCornerShape(16.dp))
                            .clickable { coverImagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isNewCoverSelected && uiState.coverImageUri != null) {
                            // Show newly selected image
                            Image(
                                painter = rememberAsyncImagePainter(uiState.coverImageUri),
                                contentDescription = "Cover Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (uiState.coverImage != null) {
                            // Show existing image
                            Image(
                                bitmap = uiState.coverImage!!.asImageBitmap(),
                                contentDescription = "Cover Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Show plus icon
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Cover",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nama Destinasi field
                    Text(
                        text = "Nama Destinasi",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Masukkan nama destinasi") },
                        isError = uiState.nameError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    if (uiState.nameError != null) {
                        Text(
                            text = uiState.nameError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alamat Destinasi field
                    Text(
                        text = "Alamat Destinasi",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.updateAddress(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Masukkan alamat destinasi") },
                        isError = uiState.addressError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    if (uiState.addressError != null) {
                        Text(
                            text = uiState.addressError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Deskripsi field
                    Text(
                        text = "Deskripsi",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Masukkan deskripsi destinasi") },
                        isError = uiState.descriptionError != null,
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    if (uiState.descriptionError != null) {
                        Text(
                            text = uiState.descriptionError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Foto Tempat Destinasi
                    Text(
                        text = "Foto Tempat Destinasi",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tampilkan gambar yang sudah ada
                        val existingPictures = uiState.pictures.filter { pictureId ->
                            !uiState.removedPictureIds.contains(pictureId.id)
                        }
                        val maxNewPictures = 3 - existingPictures.size

                        // Tampilkan gambar yang sudah ada
                        existingPictures.forEach { picture ->
                            val bitmap = uiState.pictureImages[picture.id]

                            if (bitmap != null) {
                                ExistingPictureItem(
                                    bitmap = bitmap,
                                    onRemove = { viewModel.removePicture(picture.id) }
                                )
                            }
                        }

                        // Tampilkan gambar baru yang ditambahkan
                        uiState.pictureImageUris.forEach { uri ->
                            NewPictureItem(
                                uri = uri,
                                onRemove = { viewModel.removePictureImage(uri) }
                            )
                        }

                        // Box untuk menambahkan gambar baru
                        if (existingPictures.size + uiState.pictureImageUris.size < 3) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { pictureImagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Gambar",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Black
                                )
                            }
                        }
                    }

                    // Menampilkan gambar yang akan dihapus (jika ada)
                    if (uiState.removedPictureIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Gambar yang Akan Dihapus",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, top = 8.dp),
                            textAlign = TextAlign.Left,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.removedPictureIds.forEach { pictureId ->
                                val bitmap = uiState.pictureImages[pictureId]

                                if (bitmap != null) {
                                    RemovedPictureItem(
                                        bitmap = bitmap,
                                        onUndo = { viewModel.undoRemovePicture(pictureId) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // URL Lokasi field
                    Text(
                        text = "Url Lokasi",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    OutlinedTextField(
                        value = uiState.urlLocation,
                        onValueChange = { viewModel.updateUrlLocation(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Masukkan URL Google Maps") },
                        isError = uiState.urlLocationError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    if (uiState.urlLocationError != null) {
                        Text(
                            text = uiState.urlLocationError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    // YouTube URL field
                    Text(
                        text = "URL Video YouTube (Opsional)",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    OutlinedTextField(
                        value = uiState.youtubeUrl,
                        onValueChange = { viewModel.updateYoutubeUrl(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Contoh: https://youtube.com/watch?v=xxxxx",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        },
                        isError = uiState.youtubeUrlError != null,
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    if (uiState.youtubeUrlError != null) {
                        Text(
                            text = uiState.youtubeUrlError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    // Helper text untuk YouTube URL
                    Text(
                        text = "Masukkan link video YouTube tentang destinasi ini",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Save button
                    Button(
                        onClick = { viewModel.updateDestination(onSuccess) },
                        modifier = Modifier
                            .widthIn(min = 120.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "SAVE",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
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
            }
        )
    }
}

@Composable
fun ExistingPictureItem(
    bitmap: Bitmap,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Existing Picture",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun NewPictureItem(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "New Picture",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RemovedPictureItem(
    bitmap: Bitmap,
    onUndo: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Removed Picture",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f),
            contentScale = ContentScale.Crop
        )

        // Undo button
        TextButton(
            onClick = onUndo,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        ) {
            Text(
                text = "Batal",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}