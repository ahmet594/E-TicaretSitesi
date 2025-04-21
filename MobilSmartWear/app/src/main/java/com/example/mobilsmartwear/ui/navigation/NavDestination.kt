package com.example.mobilsmartwear.ui.navigation

sealed class NavDestination(val route: String) {
    object Home : NavDestination("home")
    object ProductDetail : NavDestination("product_detail/{productId}") {
        fun createRoute(productId: String): String {
            return "product_detail/$productId"
        }
    }
    object Search : NavDestination("search")
    object Cart : NavDestination("cart")
    object Favorites : NavDestination("favorites")
    object Profile : NavDestination("profile")
    
    // Kimlik doğrulama ekranları
    object Login : NavDestination("login")
    object Register : NavDestination("register")
} 