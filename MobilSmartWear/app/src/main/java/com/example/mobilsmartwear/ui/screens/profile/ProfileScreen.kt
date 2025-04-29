package com.example.mobilsmartwear.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobilsmartwear.ui.navigation.NavDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilsmartwear.ui.screens.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Debug için kullanıcı durumunu logla
    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "İlk yükleme: userState=${userState}")
        
        if (!viewModel.isUserLoggedIn()) {
            Log.d("ProfileScreen", "Kullanıcı giriş yapmamış, giriş sayfasına yönlendiriliyor")
            navController.navigate(NavDestination.Login.route) {
                popUpTo(NavDestination.Profile.route) { inclusive = true }
            }
        } else {
            // Kullanıcı giriş yapmışsa, user state'i yenile
            Log.d("ProfileScreen", "Kullanıcı giriş yapmış, verileri yenileniyor")
            viewModel.refreshUserWithAddress()
        }
    }
    
    // userState değiştiğinde log
    LaunchedEffect(userState) {
        Log.d("ProfileScreen", "userstate değişti: ${userState?.name ?: "Null"}, Email: ${userState?.email ?: "Null"}")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Profilim",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Profil Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (userState != null) {
                        Text(
                            text = userState?.name ?: "",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userState?.email ?: "",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Kullanıcı bilgileri yüklenene kadar gösterilecek yükleniyor göstergesi
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(8.dp)
                        )
                        Text(
                            text = "Kullanıcı bilgileri yükleniyor...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menü Öğeleri
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    MenuButton(
                        icon = Icons.Default.ShoppingCart,
                        text = "Siparişlerim",
                        onClick = { 
                            navController.navigate(NavDestination.Orders.route) {
                                // Profil sayfasını backstack'te tut
                                launchSingleTop = true
                            }
                        }
                    )
                    
                    MenuButton(
                        icon = Icons.Default.Favorite,
                        text = "Favorilerim",
                        onClick = { 
                            navController.navigate(NavDestination.Favorites.route) {
                                // Profil sayfasını backstack'te tut
                                launchSingleTop = true
                            }
                        }
                    )
                    
                    MenuButton(
                        icon = Icons.Default.LocationOn,
                        text = "Adres",
                        onClick = { 
                            navController.navigate(NavDestination.Adres.route) {
                                // Profil sayfasını backstack'te tut
                                launchSingleTop = true
                            }
                        }
                    )
                    
                    MenuButton(
                        icon = Icons.Default.Lock,
                        text = "Şifre Değiştir",
                        onClick = { 
                            navController.navigate(NavDestination.ChangePassword.route) {
                                // Profil sayfasını backstack'te tut
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Çıkış Yap Butonu
            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.Profile.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Çıkış Yap",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Çıkış Yap",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "İleri",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 