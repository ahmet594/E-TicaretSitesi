package com.example.mobilsmartwear.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import android.app.Application
import com.example.mobilsmartwear.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onAddToCart: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel(
        factory = ProductDetailViewModel.provideFactory(
            application = LocalContext.current.applicationContext as Application,
            productId = productId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val scrollState = rememberScrollState()
    var selectedSize by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Ürün Görseli
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.product?.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = uiState.product?.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                                    contentDescription = "Görsel Yüklenemedi",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    )
                }

                // Ürün Bilgileri
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Ürün Adı
                    Text(
                        text = uiState.product?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fiyat
                    Text(
                        text = "${uiState.product?.price ?: 0} ₺",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Beden Seçimi - Aksesuar kategorisi için gösterilmeyecek
                    if (uiState.product?.category?.lowercase() != "aksesuar") {
                        Text(
                            text = "Beden",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Beden Butonları
                        val isShoeCategory = uiState.product?.category?.lowercase() == "ayakkabı"
                        
                        if (isShoeCategory) {
                            // Ayakkabı bedenleri için grid düzeni
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val sizes = listOf("36", "37", "38", "39", "40", "41", "42", "43", "44")
                                for (i in sizes.indices step 3) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        sizes.subList(i, minOf(i + 3, sizes.size)).forEach { size ->
                                            OutlinedButton(
                                                onClick = { 
                                                    selectedSize = size
                                                    viewModel.selectSize(size)
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (selectedSize == size) 
                                                        MaterialTheme.colorScheme.primary 
                                                    else 
                                                        MaterialTheme.colorScheme.surface
                                                )
                                            ) {
                                                Text(
                                                    text = size,
                                                    fontSize = 16.sp,
                                                    color = if (selectedSize == size) 
                                                        MaterialTheme.colorScheme.onPrimary 
                                                    else 
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        // Satırın son elemanlarını tamamlamak için boş weight
                                        if (i + 3 > sizes.size) {
                                            repeat(3 - (sizes.size - i)) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Diğer kategoriler için normal beden düzeni
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val sizes = uiState.product?.sizes ?: listOf("S", "M", "L", "XL")
                                sizes.forEach { size ->
                                    OutlinedButton(
                                        onClick = { 
                                            selectedSize = size
                                            viewModel.selectSize(size)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (selectedSize == size) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = size,
                                            color = if (selectedSize == size) 
                                                MaterialTheme.colorScheme.onPrimary 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Ürün Açıklaması
                    Text(
                        text = "Ürün Detayları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.product?.description ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sepete Ekle Butonu
                    Button(
                        onClick = { 
                            viewModel.addToCart()
                            onAddToCart()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = uiState.product?.category?.lowercase() == "aksesuar" || selectedSize != null
                    ) {
                        Text(
                            text = "SEPETE EKLE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
} 