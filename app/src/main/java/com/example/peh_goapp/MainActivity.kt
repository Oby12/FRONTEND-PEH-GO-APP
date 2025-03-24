//package com.example.peh_goapp
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.peh_goapp.ui.theme.PEHGOAPPTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            PEHGOAPPTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    PEHGOAPPTheme {
//        Greeting("Android")
//    }
//}

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background images
        Image(
            painter = painterResource(id = R.Drawable.top_illustration),
            contentDescription = "Top Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            contentScale = ContentScale.FillWidth
        )

        Image(
            painter = painterResource(id = R.drawable.bottom_waves),
            contentDescription = "Bottom Waves",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        // Login Card
        Card(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 180.dp, bottom = 24.dp)
                .align(Alignment.Center)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LOGIN Header
                Text(
                    text = "LOGIN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Email Field
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("enter your email address") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = Color.Gray
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Password Field
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    placeholder = { Text("enter your password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = Color.Gray
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Login Button
                Button(
                    onClick = { /* TODO: Handle login */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CD964) // Green button color
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // OR Divider
                Text(
                    text = "OR",
                    modifier = Modifier.padding(vertical = 16.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Google Login Button
                IconButton(
                    onClick = { /* TODO: Handle Google login */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google Login",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Register Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "don't have an account? ",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    TextButton(onClick = { /* TODO: Navigate to Register */ }) {
                        Text(
                            text = "Register",
                            fontSize = 14.sp,
                            color = Color.Blue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}