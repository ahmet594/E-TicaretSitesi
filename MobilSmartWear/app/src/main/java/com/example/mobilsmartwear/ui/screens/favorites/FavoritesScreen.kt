package com.example.mobilsmartwear.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.ui.components.ProductCard
import com.example.mobilsmartwear.ui.navigation.NavDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onProductClick: (Product) -> Unit,
    onBackClick: () -> Unit,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val favorites by favoritesViewModel.favorites.collectAsState()
    val isLoading by favoritesViewModel.isLoading.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorilerim") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    if (favorites.isNotEmpty()) {
                        // Tüm favorileri temizleme butonu
                        IconButton(onClick = { showConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Tümünü Temizle"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (favorites.isEmpty()) {
                // Favori ürün yoksa mesaj gösteriyoruz
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Favori ürününüz bulunmamaktadır",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onBackClick
                    ) {
                        Text("Alışverişe Başla")
                    }
                }
            } else {
                // Favori ürünleri grid olarak gösteriyoruz
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favorites) { product ->
                        FavoriteProductItem(
                            product = product,
                            onProductClick = { onProductClick(product) },
                            onRemoveClick = { favoritesViewModel.removeFromFavorites(product.id) }
                        )
                    }
                }
            }
        }
        
        // Tüm favorileri silme onay dialogu
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Tüm Favorileri Sil") },
                text = { Text("Tüm favori ürünlerinizi silmek istediğinize emin misiniz?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            favoritesViewModel.clearAllFavorites()
                            showConfirmDialog = false
                        }
                    ) {
                        Text("Evet, Sil")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDialog = false }
                    ) {
                        Text("İptal")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteProductItem(
    product: Product,
    onProductClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        onClick = onProductClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Box {
            ProductCard(
                product = product,
                onClick = onProductClick
            )
            
            // Kaldırma butonu
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Favorilerden Çıkar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 