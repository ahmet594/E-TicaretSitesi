package com.example.mobilsmartwear

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobilsmartwear.data.local.UserPreferences
import com.example.mobilsmartwear.data.remote.TokenManager
import com.example.mobilsmartwear.data.repository.AuthRepository
import com.example.mobilsmartwear.di.AppModule
import com.example.mobilsmartwear.ui.navigation.NavDestination
import com.example.mobilsmartwear.ui.navigation.NavGraph
import com.example.mobilsmartwear.ui.theme.MobilSmartWearTheme
import com.example.mobilsmartwear.ui.theme.NavyBlue
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "Initializing TokenManager")
            TokenManager.init(applicationContext)
            
            // RetrofitClient'ı başlat
            Log.d(TAG, "Initializing RetrofitClient")
            com.example.mobilsmartwear.data.remote.RetrofitClient.init(applicationContext)
            
            // Oturum doğrulama kontrolü
            checkLoginStatus()
            
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
    }
    
    /**
     * Uygulama başladığında kullanıcının giriş durumunu kontrol eder
     */
    private fun checkLoginStatus() {
        try {
            val userPreferences = UserPreferences(applicationContext)
            val token = userPreferences.getToken()
            val user = userPreferences.getUser()
            
            Log.d(TAG, "Oturum durumu kontrolü: Token ${if (token.isNotEmpty()) "mevcut" else "yok"}, " +
                       "Kullanıcı ${if (user != null) "mevcut (${user.name})" else "yok"}")
            
            // Eğer token varsa ama kullanıcı bilgisi yoksa veya tersi durum varsa
            if ((token.isNotEmpty() && user == null) || (token.isEmpty() && user != null)) {
                Log.w(TAG, "Tutarsız oturum durumu tespit edildi, oturum bilgileri temizleniyor")
                userPreferences.clearUserData()
            }
            
            val authRepository = AppModule.provideAuthRepository(applicationContext)
            val isLoggedIn = authRepository.isUserLoggedIn()
            Log.d(TAG, "AuthRepository.isUserLoggedIn(): $isLoggedIn")
            
        } catch (e: Exception) {
            Log.e(TAG, "Oturum durumu kontrolünde hata", e)
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    
    // Mevcut rota için state tutma
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: NavDestination.Home.route
    
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
            name = "Kombin Yap",
            route = NavDestination.Combination.route,
            icon = Icons.Default.Style
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
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = NavyBlue
            ) {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        label = { Text(item.name) },
                        selected = currentRoute == item.route,
                        onClick = { 
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { homeRoute ->
                                    popUpTo(homeRoute) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = NavyBlue.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // Asistan sayfasında FAB'ı gizle
            if (currentRoute != NavDestination.Chat.route) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(NavDestination.Chat.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "SmartWear Asistanı",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Asistan",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
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