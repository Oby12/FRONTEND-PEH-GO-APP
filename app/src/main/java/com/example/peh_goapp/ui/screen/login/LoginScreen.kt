package com.example.peh_goapp.ui.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.peh_goapp.R
import com.example.peh_goapp.ui.theme.PEHGOAPPTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Box sebagai container utama
    Box(modifier = Modifier.fillMaxSize()) {
        // Dekorasi bawah (ditempatkan pertama agar muncul di belakang)
        Image(
            painter = painterResource(id = R.drawable.logo_chamring),
            contentDescription = "Bottom Decoration",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        // Konten utama (ditempatkan setelah dekorasi agar tampil di atas)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gambar ilustrasi
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.top_illustration),
                    contentDescription = "Login Illustration",
                    modifier = Modifier.size(250.dp)
                )
            }

            // Card login
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .offset(y = (-40).dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "LOGIN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Email Field
                    Text(
                        text = "Email Address",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        placeholder = { Text("enter your email address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        isError = uiState.emailError != null
                    )
                    if (uiState.emailError != null) {
                        Text(
                            text = uiState.emailError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        placeholder = { Text("enter your password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password Icon"
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = uiState.passwordError != null
                    )
                    if (uiState.passwordError != null) {
                        Text(
                            text = uiState.passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button
                    Button(
                        onClick = { viewModel.login(onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
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
                                text = "Login",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // OR text
                    Text(
                        text = "OR",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.Gray
                    )

                    // Register link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "don't have an account? ",
                            color = Color.Gray
                        )
                        Text(
                            text = "Register",
                            color = Color.Blue,
                            modifier = Modifier.clickable { onRegisterClick() }
                        )
                    }
                }
            }

            // Spacer untuk memberikan jarak di bagian bawah card
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Show error dialog if needed
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

//@Preview(showBackground = true)
//@Composable
//fun LoginScreenPreview() {
//    // Buat dummy viewModel untuk preview
//    class DummyLoginViewModel : LoginViewModel(FakeUserRepository()) {
//        override val uiState = MutableStateFlow(LoginUiState()).asStateFlow()
//        override fun updateEmail(email: String) {}
//        override fun updatePassword(password: String) {}
//        override fun login(onLoginSuccess: () -> Unit) {}
//        override fun clearError() {}
//    }
//
//    PehGoAppTheme {
//        LoginScreen(
//            onLoginSuccess = {},
//            onRegisterClick = {},
//            viewModel = DummyLoginViewModel()
//        )
//    }
//}
