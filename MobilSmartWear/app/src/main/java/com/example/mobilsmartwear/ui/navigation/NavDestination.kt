package com.example.mobilsmartwear.ui.navigation

sealed class NavDestination(val route: String) {
    object Home : NavDestination("home")
    object ProductDetail : NavDestination("product_detail/{productId}") {
        fun createRoute(productId: String): String {
            return "product_detail/$productId"
        }
    }
    object Category : NavDestination("category/{categoryName}") {
        fun createRoute(categoryName: String): String {
            return "category/$categoryName"
        }
    }
    object Search : NavDestination("search")
    object Cart : NavDestination("cart")
    object Combination : NavDestination("combination")
    object Chat : NavDestination("chat")
    object Profile : NavDestination("profile")
    object Favorites : NavDestination("favorites")
    
    // Kimlik doğrulama ekranları
    object Login : NavDestination("login")
    object Register : NavDestination("register")
    
    // Profil alt sayfaları
    object Orders : NavDestination("orders")
    object Coupons : NavDestination("coupons")
    object Reviews : NavDestination("reviews")
    object Adres : NavDestination("addresses")
    object ChangePassword : NavDestination("change_password")
} 