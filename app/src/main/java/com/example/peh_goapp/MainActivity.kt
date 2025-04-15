package com.example.peh_goapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.peh_goapp.data.local.TokenPreference
import com.example.peh_goapp.data.remote.api.Base64ImageService
import com.example.peh_goapp.ui.screen.adddestination.AddDestinationScreen
import com.example.peh_goapp.ui.screen.destination.CategoryDestinationsScreen
import com.example.peh_goapp.ui.screen.destinationdetail.DestinationDetailScreen
import com.example.peh_goapp.ui.screen.login.LoginScreen
import com.example.peh_goapp.ui.screen.main.MainScreen
import com.example.peh_goapp.ui.screen.register.RegisterScreen
import com.example.peh_goapp.ui.theme.PEHGOAPPTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenPreference: TokenPreference

    @Inject
    lateinit var base64ImageService: Base64ImageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PEHGOAPPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        tokenPreference = tokenPreference,
                        base64ImageService = base64ImageService
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    tokenPreference: TokenPreference,
    base64ImageService: Base64ImageService,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            MainScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onCategoryClick = { categoryId ->
                    navController.navigate("category/$categoryId")
                },
                onScannerClick = {
                    navController.navigate("scanner")
                }
            )
        }

        // Route untuk halaman daftar destinasi berdasarkan kategori
        composable(
            route = "category/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            CategoryDestinationsScreen(
                categoryId = categoryId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onDestinationClick = { destinationId ->
                    navController.navigate("destination/$categoryId/$destinationId")
                },
                onAddDestinationClick = { catId ->
                    navController.navigate("add-destination/$catId")
                },
                tokenPreference = tokenPreference,
                base64ImageService = base64ImageService
            )
        }

        // Route untuk halaman detail destinasi
        composable(
            route = "destination/{categoryId}/{destinationId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("destinationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            val destinationId = backStackEntry.arguments?.getInt("destinationId") ?: 1

            DestinationDetailScreen(
                categoryId = categoryId,
                destinationId = destinationId,
                onNavigateBack = { navController.navigateUp() },
                onEditClick = { catId, destId ->
                    navController.navigate("edit-destination/$catId/$destId")
                },
                tokenPreference = tokenPreference,
                base64ImageService = base64ImageService
            )
        }

        // Route untuk halaman tambah destinasi
        composable(
            route = "add-destination/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            AddDestinationScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.navigateUp() },
                onSuccess = {
                    navController.navigate("category/$categoryId") {
                        popUpTo("add-destination/$categoryId") { inclusive = true }
                    }
                },
                //tokenPreference = tokenPreference
            )
        }

        composable("scanner") {
            // TODO: Implementasi halaman scanner
            // ScannerScreen(
            //     onNavigateBack = { navController.navigateUp() }
            // )
        }
    }
}