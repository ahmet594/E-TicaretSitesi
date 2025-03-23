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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                        AppNavHost()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MainActivity", e)
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { 
                    navController.navigate("home")
                },
                onRegisterClick = { navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onContinueWithoutLogin = { navController.navigate("home") }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onRegisterClick = { 
                    navController.navigate("home")
                },
                onLoginClick = { navController.navigate("login") },
                onTermsClick = { /* İleride kullanım şartları ekranına gidecek */ },
                onPrivacyClick = { /* İleride gizlilik politikası ekranına gidecek */ }
            )
        }
        
        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSendResetLinkClick = { /* Şimdilik işlev yok */ },
                onLoginClick = { navController.navigate("login") { popUpTo("login") { inclusive = true } } }
            )
        }
        
        composable("home") {
            HomeScreen(navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    MobilSmartWearTheme {
        AppNavHost()
    }
}