package com.example.mobilsmartwear.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mobilsmartwear.ui.screens.auth.LoginScreen
import com.example.mobilsmartwear.ui.screens.auth.RegisterScreen
import com.example.mobilsmartwear.ui.screens.cart.CartScreen
import com.example.mobilsmartwear.ui.screens.category.CategoryScreen
import com.example.mobilsmartwear.ui.screens.favorites.FavoritesScreen
import com.example.mobilsmartwear.ui.screens.home.HomeScreen
import com.example.mobilsmartwear.ui.screens.product.ProductDetailScreen
import com.example.mobilsmartwear.ui.screens.profile.AdresScreen
import com.example.mobilsmartwear.ui.screens.profile.OrdersScreen
import com.example.mobilsmartwear.ui.screens.profile.ProfileScreen
import com.example.mobilsmartwear.ui.screens.search.SearchScreen
import com.example.mobilsmartwear.ui.screens.outfit.OutfitScreen
import com.example.mobilsmartwear.ui.screens.chat.ChatScreen
import androidx.compose.material3.Text
import com.example.mobilsmartwear.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Ana ekranlar
        composable(route = NavDestination.Home.route) {
            val context = LocalContext.current
            val productRepository = AppModule.provideProductRepository(context.applicationContext as android.app.Application)
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate("${NavDestination.ProductDetail.route}/$productId")
                },
                onCategoryClick = { category ->
                    navController.navigate(NavDestination.Category.createRoute(category))
                },
                onSearchClick = {
                    navController.navigate(NavDestination.Search.route)
                },
                onFavoriteClick = {
                    navController.navigate(NavDestination.Favorites.route)
                },
                productRepository = productRepository
            )
        }
        
        // Kategori sayfası
        composable(
            route = NavDestination.Category.route,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryScreen(
                category = category,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate("${NavDestination.ProductDetail.route}/$productId")
                }
            )
        }
        
        composable(route = NavDestination.Search.route) {
            SearchScreen(
                onProductClick = { productId ->
                    navController.navigate("${NavDestination.ProductDetail.route}/$productId")
                }
            )
        }
        
        composable(route = NavDestination.Combination.route) {
            val context = LocalContext.current
            val cartRepository = AppModule.provideCartRepository(context.applicationContext as android.app.Application)
            
            OutfitScreen(
                onProductClick = { productId ->
                    navController.navigate("${NavDestination.ProductDetail.route}/$productId")
                },
                onAddToCart = { product ->
                    // Gerçek sepete ekleme işlemi
                    CoroutineScope(Dispatchers.IO).launch {
                        cartRepository.addToCart(product, quantity = 1)
                    }
                    // Cart sayfasına yönlendir
                    navController.navigate(NavDestination.Cart.route)
                }
            )
        }
        
        composable(route = NavDestination.Favorites.route) {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(NavDestination.ProductDetail.createRoute(productId))
                }
            )
        }
        
        composable(route = NavDestination.Cart.route) {
            val context = LocalContext.current
            val cartViewModel: com.example.mobilsmartwear.ui.screens.cart.CartViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(context.applicationContext as android.app.Application)
            )
            
            CartScreen(
                viewModel = cartViewModel,
                onNavigateToLogin = {
                    navController.navigate(NavDestination.Login.route)
                },
                onNavigateToOrders = {
                    navController.navigate(NavDestination.Orders.route)
                }
            )
        }
        
        composable(route = NavDestination.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(
            route = "${NavDestination.ProductDetail.route}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onAddToCart = {
                    navController.navigate(NavDestination.Cart.route)
                }
            )
        }
        
        // Kimlik doğrulama ekranları
        composable(route = NavDestination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(NavDestination.Register.route)
                }
            )
        }
        
        composable(route = NavDestination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Profil alt sayfaları
        composable(route = NavDestination.Orders.route) {
            OrdersScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(route = NavDestination.Coupons.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Kuponlarım Sayfası - Yapım Aşamasında")
            }
        }
        
        composable(route = NavDestination.Reviews.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Değerlendirmelerim Sayfası - Yapım Aşamasında")
            }
        }
        
        composable(route = NavDestination.Adres.route) {
            AdresScreen(navController = navController)
        }
        
        composable(route = NavDestination.ChangePassword.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Şifre Değiştirme Sayfası - Yapım Aşamasında")
            }
        }
        
        composable(route = NavDestination.Chat.route) {
            ChatScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 