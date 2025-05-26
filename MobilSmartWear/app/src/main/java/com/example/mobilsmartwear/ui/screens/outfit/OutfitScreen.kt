package com.example.mobilsmartwear.ui.screens.outfit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilsmartwear.ui.viewmodel.OutfitViewModel
import com.example.mobilsmartwear.data.remote.api.OutfitResponse
import com.example.mobilsmartwear.ui.components.ProductCard
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.util.CombinationManager
import com.example.mobilsmartwear.util.OutfitMatchResult
import com.example.mobilsmartwear.util.SeasonDetector
import com.example.mobilsmartwear.util.Season
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable

@Composable
fun OutfitScreen(
    viewModel: OutfitViewModel = viewModel(),
    onProductClick: (String) -> Unit = {},
    onAddToCart: (Product) -> Unit = {}
) {
    val outfits by viewModel.outfits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedProducts by CombinationManager.selectedProducts.collectAsState()
    val matchResults by CombinationManager.matchResults.collectAsState()
    
    // Mevsim filtreleme state'i
    var selectedSeasonFilter by remember { mutableStateOf<Season?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Seçili Ürünler Bölümü
        if (selectedProducts.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Seçili Ürünler (${selectedProducts.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        TextButton(
                            onClick = { CombinationManager.clearAll() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Temizle",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Temizle", fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LazyRow(
                        horizontalArrangement = if (selectedProducts.size == 1) 
                            Arrangement.Center 
                        else 
                            Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedProducts) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) },
                                modifier = Modifier.width(100.dp),
                                isCompact = true,
                                showRemoveButton = true,
                                onRemoveClick = { 
                                    CombinationManager.removeProduct(product.id, outfits, viewModel.getAllCachedProducts())
                                },
                                isProblematic = matchResults.problematicProductId == product.id
                            )
                        }
                    }
                    
                    // Problematik ürün uyarısı
                    if (matchResults.problematicProductId != null) {
                        val problematicProduct = selectedProducts.find { it.id == matchResults.problematicProductId }
                        if (problematicProduct != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "${problematicProduct.name} ürünü diğer seçimlerinizle daha az eşleşme sağlıyor.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Bu ürünü değiştirerek daha fazla kombin seçeneği bulabilirsiniz.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Kombinle Butonu
                    Button(
                        onClick = { 
                            val allProducts = viewModel.getAllCachedProducts()
                            CombinationManager.findMatchingOutfitsWithSeasonFilter(outfits, allProducts, selectedSeasonFilter)
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UYUMLU KOMBİNLERİ BUL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Mevsim Filtreleme - Seçili ürün varsa her zaman göster
                    if (selectedProducts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "🌤️ Mevsim Filtresi:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tümü butonu
                            item {
                                ElevatedButton(
                                    onClick = { 
                                        selectedSeasonFilter = null
                                        val allProducts = viewModel.getAllCachedProducts()
                                        CombinationManager.findMatchingOutfitsWithSeasonFilter(outfits, allProducts, null)
                                    },
                                    modifier = Modifier.height(42.dp),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = if (selectedSeasonFilter == null) 
                                            MaterialTheme.colorScheme.primary
                                        else 
                                            MaterialTheme.colorScheme.surface,
                                        contentColor = if (selectedSeasonFilter == null) 
                                            MaterialTheme.colorScheme.onPrimary
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    elevation = ButtonDefaults.elevatedButtonElevation(
                                        defaultElevation = if (selectedSeasonFilter == null) 6.dp else 2.dp
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = "🌍",
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tümü",
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedSeasonFilter == null) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            // Mevsim butonları
                            items(listOf(
                                Triple(Season.WINTER, "❄️", Color(0xFF1976D2)),
                                Triple(Season.SPRING, "🌸", Color(0xFF388E3C)), 
                                Triple(Season.SUMMER, "☀️", Color(0xFFF57C00))
                            )) { (season, emoji, color) ->
                                ElevatedButton(
                                    onClick = { 
                                        selectedSeasonFilter = season
                                        val allProducts = viewModel.getAllCachedProducts()
                                        CombinationManager.findMatchingOutfitsWithSeasonFilter(outfits, allProducts, season)
                                    },
                                    modifier = Modifier.height(42.dp),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = if (selectedSeasonFilter == season) 
                                            color
                                        else 
                                            MaterialTheme.colorScheme.surface,
                                        contentColor = if (selectedSeasonFilter == season) 
                                            Color.White
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    elevation = ButtonDefaults.elevatedButtonElevation(
                                        defaultElevation = if (selectedSeasonFilter == season) 6.dp else 2.dp
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = season.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedSeasonFilter == season) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Eşleşme Sonuçları
        if (matchResults.hasResults) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sadece Tam Eşleşmeler
                if (matchResults.perfectMatches.isNotEmpty()) {
                    item {
                        OutfitMatchSection(
                            title = "🎯 Tam Eşleşen Kombinler (${matchResults.perfectMatches.size})",
                            subtitle = "Seçtiğiniz ${matchResults.selectedProductCount} ürünün tamamını içeren kombinler",
                            outfits = matchResults.perfectMatches,
                            viewModel = viewModel,
                            onProductClick = onProductClick,
                            onAddToCart = onAddToCart,
                            isPerfectMatch = true
                        )
                    }
                }
            }
        }
        // Seçili ürün var ama sonuç yok
        else if (selectedProducts.isNotEmpty() && !matchResults.hasResults && outfits.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val currentSelectedSeason = matchResults.selectedSeason
                    Text(
                        text = if (currentSelectedSeason != null) 
                            "${SeasonDetector.getSeasonDisplay(currentSelectedSeason)} Mevsimi İçin Kombin Bulunamadı"
                        else
                            "Tam Eşleşen Kombin Bulunamadı",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentSelectedSeason != null)
                            "${SeasonDetector.getSeasonDisplay(currentSelectedSeason)} kombinleri bulunamadı.\nFarklı mevsim filtresi deneyin."
                        else
                            "Uyumlu kombin bulunamadı.\nFarklı ürünler deneyin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    if (currentSelectedSeason != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                selectedSeasonFilter = null
                                val allProducts = viewModel.getAllCachedProducts()
                                CombinationManager.findMatchingOutfitsWithSeasonFilter(outfits, allProducts, null)
                            }
                        ) {
                            Text("🌍 Tüm Mevsimlerde Ara")
                        }
                    }
                }
            }
        }
        // Orijinal kombinler bölümü (seçili ürün yoksa veya sonuç yoksa)
        else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error ?: "Bilinmeyen bir hata oluştu",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadOutfits() }) {
                                Text("Tekrar Dene")
                            }
                        }
                    }
                    selectedProducts.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Kombin Oluşturun",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ürün detay sayfalarından 'Kombinle' butonuna tıklayarak ürün seçin ve uyumlu kombinleri keşfedin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        // Seçili ürün var ama henüz arama yapılmamış
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Uyumlu Kombinleri Bul",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Seçtiğiniz ${selectedProducts.size} ürünün tamamını içeren kombinleri bulmak için yukarıdaki 'UYUMLU KOMBİNLERİ BUL' butonuna tıklayın",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitRow(
    outfit: OutfitResponse,
    products: List<Product>,
    onProductClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun OutfitMatchSection(
    title: String,
    subtitle: String,
    outfits: List<OutfitResponse> = emptyList(),
    partialMatches: List<Pair<OutfitResponse, Int>> = emptyList(),
    viewModel: OutfitViewModel,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    isPerfectMatch: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPerfectMatch) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Tam eşleşmeler için
            if (isPerfectMatch && outfits.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    outfits.forEach { outfit ->
                        OutfitMatchRow(
                            outfit = outfit,
                            products = viewModel.getProductsForOutfit(outfit),
                            onProductClick = onProductClick,
                            onAddToCart = onAddToCart,
                            matchInfo = "Tam Eşleşme",
                            matchColor = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            // Kısmi eşleşmeler için
            if (!isPerfectMatch && partialMatches.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    partialMatches.forEach { (outfit, matchCount) ->
                        OutfitMatchRow(
                            outfit = outfit,
                            products = viewModel.getProductsForOutfit(outfit),
                            onProductClick = onProductClick,
                            onAddToCart = onAddToCart,
                            matchInfo = "$matchCount ürün eşleşiyor",
                            matchColor = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitMatchRow(
    outfit: OutfitResponse,
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    matchInfo: String,
    matchColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ürün kartları
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(products) { product ->
                    val isSelected = CombinationManager.isProductSelected(product.id)
                    Box {
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            modifier = Modifier.width(140.dp),
                            showAddToCartButton = true,
                            onAddToCartClick = {
                                onAddToCart(product)
                            }
                        )
                        
                        // Seçili ürün işareti
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Seçili",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 