package com.example.peh_goapp

import android.os.Bundle
import android.util.Log
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
import com.example.peh_goapp.ui.screen.editdestination.EditDestinationScreen
import com.example.peh_goapp.ui.screen.favorite.FavoriteScreen
import com.example.peh_goapp.ui.screen.info.InfoScreen
import com.example.peh_goapp.ui.screen.introduction.IntroductionScreen
import com.example.peh_goapp.ui.screen.login.LoginScreen
import com.example.peh_goapp.ui.screen.main.MainScreen
import com.example.peh_goapp.ui.screen.register.RegisterScreen
import com.example.peh_goapp.ui.screen.scanner.ScannerScreen
import com.example.peh_goapp.ui.screen.scanresult.ScanResultScreen
import com.example.peh_goapp.ui.screen.splash.SplashScreen
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

                    // Selalu mulai dari splash screen
                    AppNavHost(
                        navController = navController,
                        tokenPreference = tokenPreference,
                        base64ImageService = base64ImageService,
                        startDestination = "splash"
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
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash Screen - selalu menjadi entry point pertama
        composable("splash") {
            SplashScreen(
                onNavigateToIntroduction = {
                    navController.navigate("introduction") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Introduction Screen - hanya ditampilkan pada first time
        composable("introduction") {
            IntroductionScreen(
                onFinishIntroduction = {
                    navController.navigate("login") {
                        popUpTo("introduction") { inclusive = true }
                    }
                }
            )
        }

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
                },
                onNavigateToInformation = {
                    Log.d("AppNavHost", "Memanggil navController.navigate('information')")
                    navController.navigate("information")
                },
                onNavigateToFavorites = {
                    navController.navigate("favorites")
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
                    // Pastikan kembali langsung ke home
                    navController.popBackStack(route = "home", inclusive = false)
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
                onNavigateBack = {
                    // Kembali ke halaman kategori
                    navController.popBackStack()
                },
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
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    // Pastikan navigasi kembali ke halaman kategori dan hapus halaman add dari stack
                    navController.navigate("category/$categoryId") {
                        popUpTo("category/$categoryId") { inclusive = true }
                    }
                }
            )
        }

        // Route untuk halaman scanner QR code
        composable("scanner") {
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onScanSuccess = { categoryId, destinationId ->
                    // Navigasi ke halaman hasil scan
                    navController.navigate("scan-result/$categoryId/$destinationId")
                }
            )
        }

        // Route untuk halaman hasil scan QR code
        composable(
            route = "scan-result/{categoryId}/{destinationId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("destinationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            val destinationId = backStackEntry.arguments?.getInt("destinationId") ?: 1

            ScanResultScreen(
                categoryId = categoryId,
                destinationId = destinationId,
                onNavigateBack = {
                    // Kembali ke home
                    navController.navigate("home") {
                        popUpTo("scanner") { inclusive = true }
                    }
                }
            )
        }

        // Route untuk halaman edit destinasi
        composable(
            route = "edit-destination/{categoryId}/{destinationId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("destinationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            val destinationId = backStackEntry.arguments?.getInt("destinationId") ?: 1
            EditDestinationScreen(
                categoryId = categoryId,
                destinationId = destinationId,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    // Navigasi langsung ke halaman kategori setelah edit sukses
                    // Dan hapus semua halaman sebelumnya kecuali home
                    navController.navigate("category/$categoryId") {
                        // Hapus semua destinasi di atas home dari back stack
                        popUpTo("home") {
                            // Jangan hapus home itu sendiri
                            inclusive = false
                            // Simpan state home
                            saveState = true
                        }
                        // Pastikan kategori dimuat ulang sepenuhnya
                        restoreState = false
                        // Hindari duplikasi destinasi kategori
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("information"){
            InfoScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        //halaman favorite
        composable("favorites") {
            FavoriteScreen(
                onNavigateBack = { navController.popBackStack() },
                onDestinationClick = { destinationId ->
                    // Default categoryId = 1 (Destination/Tour)
                    navController.navigate("destination/1/$destinationId")
                },
                tokenPreference = tokenPreference,
                base64ImageService = base64ImageService
            )
        }
    }
}