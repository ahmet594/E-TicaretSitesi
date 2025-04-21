package com.example.mobilsmartwear.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobilsmartwear.data.remote.TokenManager
import com.example.mobilsmartwear.ui.navigation.NavDestination

@Composable
fun ProfileScreen(
    navController: NavController
) {
    // Kullanıcının giriş yapıp yapmadığını kontrol et
    val isLoggedIn = TokenManager.isLoggedIn()
    
    // Eğer kullanıcı giriş yapmamışsa giriş sayfasına yönlendir
    LaunchedEffect(key1 = isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(NavDestination.Login.route) {
                popUpTo(NavDestination.Profile.route) { inclusive = true }
            }
        }
    }
    
    // Eğer kullanıcı giriş yapmışsa profil sayfasını göster
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Profil Ekranı",
            fontSize = 24.sp
        )
    }
} 