package com.example.mobilsmartwear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobilsmartwear.data.remote.TokenManager
import com.example.mobilsmartwear.ui.screens.home.HomeScreen
import com.example.mobilsmartwear.ui.theme.MobilSmartWearTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobilsmartwear.ui.navigation.NavDestination
import com.example.mobilsmartwear.ui.screens.cart.CartScreen
import com.example.mobilsmartwear.ui.screens.product.ProductDetailScreen
import androidx.navigation.NavHostController
import com.example.mobilsmartwear.ui.screens.auth.ForgotPasswordScreen
import com.example.mobilsmartwear.ui.screens.auth.LoginScreen
import com.example.mobilsmartwear.ui.screens.auth.RegisterScreen
import com.example.mobilsmartwear.ui.screens.favorites.FavoritesScreen
import com.example.mobilsmartwear.ui.components.BottomNavigationBar
import com.example.mobilsmartwear.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Uygulama başlatılıyor...")
        
        try {
            // TokenManager'ı başlat
            Log.d(TAG, "Initializing TokenManager")
            TokenManager.init(applicationContext)
            
            enableEdgeToEdge()
            
            Log.d(TAG, "Setting up Compose UI")
            setContent {
                MobilSmartWearTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainAppContent()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MainActivity", e)
        }
        
        Log.d(TAG, "Uygulama başlatıldı")
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    
    // Bottom bar öğelerini tanımla
    val navigationItems = listOf(
        BottomNavigationItem(
            name = "Ana Sayfa",
            route = NavDestination.Home.route,
            icon = Icons.Default.Home
        ),
        BottomNavigationItem(
            name = "Arama",
            route = NavDestination.Search.route,
            icon = Icons.Outlined.Search
        ),
        BottomNavigationItem(
            name = "Favoriler",
            route = NavDestination.Favorites.route,
            icon = Icons.Outlined.FavoriteBorder
        ),
        BottomNavigationItem(
            name = "Sepet",
            route = NavDestination.Cart.route,
            icon = Icons.Default.ShoppingCart
        ),
        BottomNavigationItem(
            name = "Profil",
            route = NavDestination.Profile.route,
            icon = Icons.Default.Person
        )
    )
    
    // Login ve Register ekranlarında bottom bar gösterilmez
    val shouldShowBottomBar = true
    
    // Scaffold ile sayfa yapısını oluştur
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    items = navigationItems,
                    currentRoute = NavDestination.Home.route,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            navController.graph.startDestinationRoute?.let { homeRoute ->
                                popUpTo(homeRoute) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            paddingValues = paddingValues
        )
    }
}

data class BottomNavigationItem(
    val name: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Preview(showBackground = true)
@Composable
fun MainAppContentPreview() {
    MobilSmartWearTheme {
        MainAppContent()
    }
}