package com.example.mobilsmartwear.ui.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.ui.components.ProductCard
import com.example.mobilsmartwear.ui.navigation.NavDestination
import com.example.mobilsmartwear.ui.theme.Primary
import androidx.compose.material3.HorizontalDivider
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.example.mobilsmartwear.ui.components.HomeAppBar
import com.example.mobilsmartwear.ui.components.ErrorDialog
import com.example.mobilsmartwear.ui.components.CategoryChip


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    
    // Loglama
    val TAG = "HomeScreen"
    Log.d(TAG, "HomeScreen composable called. isLoading: $isLoading, products: ${uiState.products.size}")
    
    // UI durumunu kontrol et
    LaunchedEffect(homeViewModel) {
        Log.d(TAG, "HomeScreen LaunchedEffect triggered")
        if (uiState.products.isEmpty() && !isLoading) {
            Log.d(TAG, "Product list is empty and not loading, calling loadProducts()")
            homeViewModel.loadProducts()
        }
    }
    
    // Hata diyaloğu
    uiState.error?.let { error ->
        ErrorDialog(
            message = error,
            onDismiss = { homeViewModel.clearError() },
            onRetry = { homeViewModel.refreshProducts() }
        )
    }
    
    // SwipeRefresh durumu
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    
    Scaffold(
        topBar = {
            HomeAppBar(
                title = "SmartWear",
                onMenuClick = {},
                onSearchClick = {},
                actions = {
                    // Manuel yenileme butonu
                    IconButton(onClick = { homeViewModel.refreshProducts() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ürünleri Yenile"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // SwipeRefresh ile sarılmış ana içerik
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { homeViewModel.refreshProducts() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && uiState.products.isEmpty()) {
                // Tamamen boş ekranda loading gösterimi
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ürünler Yükleniyor...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Kategori seçimi
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(homeViewModel.categories) { category ->
                                CategoryChip(
                                    category = category,
                                    selected = category == selectedCategory,
                                    onClick = { homeViewModel.selectCategory(category) }
                                )
                            }
                        }
                    }
                    
                    // Öne çıkan ürünler
                    if (uiState.featuredProducts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Öne Çıkanlar",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                            )
                        }
                        
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.featuredProducts) { product ->
                                    ProductCard(
                                        product = product,
                                        onClick = {
                                            onProductClick(product.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Tüm ürünler
                    item {
                        Text(
                            text = when (selectedCategory) {
                                "Anasayfa" -> "Tüm Ürünler"
                                else -> selectedCategory
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                        )
                    }
                    
                    if (uiState.products.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ürün bulunamadı",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { homeViewModel.refreshProducts() }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Yenile")
                                    }
                                }
                            }
                        }
                    } else {
                        // Ürünleri listele
                        items(uiState.products) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = backgroundColor,
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = category,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
} 