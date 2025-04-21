package com.example.mobilsmartwear.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mobilsmartwear.ui.screens.auth.LoginScreen
import com.example.mobilsmartwear.ui.screens.auth.RegisterScreen
import com.example.mobilsmartwear.ui.screens.home.HomeScreen
import com.example.mobilsmartwear.ui.screens.favorites.FavoritesScreen
import com.example.mobilsmartwear.ui.screens.cart.CartScreen
import com.example.mobilsmartwear.ui.screens.product.ProductDetailScreen
import com.example.mobilsmartwear.ui.screens.profile.ProfileScreen
import com.example.mobilsmartwear.ui.screens.search.SearchScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Login.route,
        modifier = Modifier.padding(paddingValues)
    ) {
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
                },
                onContinueWithoutLogin = {
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Login.route) { inclusive = true }
                    }
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
                    navController.popBackStack()
                }
            )
        }
        
        // Ana ekranlar
        composable(route = NavDestination.Home.route) {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate("${NavDestination.ProductDetail.route}/$productId")
                }
            )
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
        
        composable(route = NavDestination.Search.route) {
            SearchScreen(
                onProductClick = { productId ->
                    navController.navigate("${NavDestination.ProductDetail.route}/$productId")
                }
            )
        }
        
        composable(route = NavDestination.Cart.route) {
            CartScreen()
        }
        
        composable(route = NavDestination.Favorites.route) {
            FavoritesScreen(
                onProductClick = { productId ->
                    navController.navigate("${NavDestination.ProductDetail.route}/$productId")
                }
            )
        }
        
        composable(route = NavDestination.Profile.route) {
            ProfileScreen(
                navController = navController
            )
        }
    }
} 