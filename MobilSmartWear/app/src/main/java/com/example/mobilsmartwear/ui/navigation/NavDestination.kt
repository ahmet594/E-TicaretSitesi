package com.example.mobilsmartwear.ui.navigation

sealed class NavDestination(val route: String) {
    object Home : NavDestination("home")
    object ProductDetail : NavDestination("product_detail")
    object Cart : NavDestination("cart")
    object Checkout : NavDestination("checkout")
    object Search : NavDestination("search")
    object Profile : NavDestination("profile")
    object Orders : NavDestination("orders")
    object Favorites : NavDestination("favorites")
    object Settings : NavDestination("settings")
} 