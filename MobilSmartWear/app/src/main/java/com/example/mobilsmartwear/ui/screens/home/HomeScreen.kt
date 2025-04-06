package com.example.mobilsmartwear.ui.screens.home

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Eğer hata varsa Snackbar'ı göster
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("MobilSmartWear", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                ),
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open navigation drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    // Giriş yap butonu
                    TextButton(
                        onClick = { navController.navigate("login") },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Giriş Yap", 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Sepet ikonu
                    IconButton(onClick = { navController.navigate(NavDestination.Cart.route) }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Sepet", tint = Color.White)
                    }
                    
                    // Favoriler ikonu
                    IconButton(onClick = { navController.navigate(NavDestination.Favorites.route) }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorilerim", tint = Color.White)
                    }
                    
                    // Arama ikonu
                    IconButton(onClick = { navController.navigate(NavDestination.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        HomeScreenContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onCategorySelected = { viewModel.selectCategory(it) },
            onProductClick = { product ->
                navController.navigate("${NavDestination.ProductDetail.route}/${product.id}")
            },
            onRefresh = { viewModel.loadProducts() }
        )
    }
}

@Composable
fun HomeScreenContent(
    paddingValues: PaddingValues,
    uiState: HomeUiState,
    onCategorySelected: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onRefresh: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    var showSorting by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Kategoriler
        CategoryRow(
            categories = listOf("Anasayfa", "Erkek", "Kadın", "Koleksiyon"),
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected
        )
        
        // Filtrele ve Sırala butonları (Kadın veya Erkek kategorisinde)
        if (uiState.selectedCategory == "Kadın" || uiState.selectedCategory == "Erkek") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Filtrele butonu
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (showFilters) Primary.copy(alpha = 0.1f) else Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .border(
                            width = 1.dp,
                            color = if (showFilters) Primary else Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { showFilters = !showFilters }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Filtrele",
                            tint = if (showFilters) Primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filtrele",
                            color = if (showFilters) Primary else Color.Black
                        )
                    }
                }
                
                // Sırala butonu
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (showSorting) Primary.copy(alpha = 0.1f) else Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .border(
                            width = 1.dp,
                            color = if (showSorting) Primary else Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { showSorting = !showSorting }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Sırala",
                            tint = if (showSorting) Primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sırala",
                            color = if (showSorting) Primary else Color.Black
                        )
                    }
                }
            }
            
            // Filtre paneli (sadece showFilters true olduğunda)
            if (showFilters) {
                if (uiState.selectedCategory == "Kadın") {
                    FiltersSection()
                } else if (uiState.selectedCategory == "Erkek") {
                    FiltersSectionErkek()
                }
            }
            
            // Sıralama paneli (sadece showSorting true olduğunda)
            if (showSorting) {
                SortingSection()
            }
        }
        
        // Öne çıkan ürünler başlığını kaldırdık
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(uiState.featuredProducts) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
        
        // Tüm ürünler başlığını kaldırdık
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                text = category,
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Primary else Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (selected) Primary else Color.LightGray,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FiltersSection() {
    val categories = listOf(
        "Giyim" to listOf(
            "Pantolon", "Şort", "Tayt", "Etek", "T-Shirt", "Sweatshirt", 
            "Ceket", "Bluz", "Tunik", "Gömlek", "Eşofman", "Mont", 
            "Trençkot", "Hırka", "Kazak", "Kaban", "Bolero", "Kot Pantolon"
        ),
        "Ayakkabı" to listOf(
            "Topuklu Ayakkabı", "Bot", "Sneakers", "Spor Ayakkabı", "Babet"
        ),
        "Aksesuar" to listOf(
            "Çanta", "Atkı", "Eldiven", "Şal", "Şapka", "Kemer"
        )
    )
    
    val bedenler = listOf("XS", "S", "M", "L", "XL", "XXL")
    val renkler = listOf(
        "Siyah", "Beyaz", "Lacivert", "Gri", "Kırmızı", 
        "Mavi", "Yeşil", "Kahverengi", "Pembe", "Mor"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Filtreler",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Kategori filtreleri
        categories.forEach { (category, subcategories) ->
            FilterCategory(
                title = category,
                items = subcategories
            )
        }
        
        // Fiyat aralığı filtresi
        Text(
            text = "Fiyat Aralığı",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            TextButton(
                onClick = { /* Filtreleri uygula */ },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Primary
                )
            ) {
                Text("Uygula")
            }
        }
        
        // Beden filtresi
        Text(
            text = "Beden",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        ChipGroup(items = bedenler)
        
        // Renk filtresi
        Text(
            text = "Renk",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        ChipGroup(items = renkler)
    }
}

@Composable
fun FilterCategory(
    title: String,
    items: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Daralt" else "Genişlet"
            )
        }
        
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { /* Filtre değerini değiştir */ }
                        )
                        
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChipGroup(items: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            var selected by remember { mutableStateOf(false) }
            
            Surface(
                color = if (selected) Primary.copy(alpha = 0.1f) else Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (selected) Primary else Color.LightGray,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { selected = !selected }
            ) {
                Text(
                    text = item,
                    color = if (selected) Primary else Color.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SortingSection() {
    val sortOptions = listOf(
        "Önerilen Sıralama",
        "Fiyat: Düşükten - Yükseğe",
        "Fiyat: Yüksekten - Düşüğe",
        "Yeni Gelenler",
        "Çok Satanlar",
        "En Yüksek Puanlılar",
        "İndirim Oranı"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Sıralama",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column {
            sortOptions.forEachIndexed { index, option ->
                val isSelected = index == 0 // Varsayılan olarak ilk seçenek seçili
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Sıralama seçeneğini değiştir */ }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { /* Sıralama seçeneğini değiştir */ }
                    )
                    
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                if (index < sortOptions.size - 1) {
                    HorizontalDivider(
                        color = Color.LightGray,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun FiltersSectionErkek() {
    val categories = listOf(
        "Giyim" to listOf(
            "Hırka", "Kaban & Parka", "Kazak", "Şortlar", "Pantolon", 
            "Ceket", "T-Shirt", "Gömlek", "Mont", "Sweatshirt", 
            "Takım Elbise", "Eşofman", "Yelek", "Kot Pantolon", "Kumaş Pantolon"
        ),
        "Ayakkabı" to listOf(
            "Spor Ayakkabı", "Sneaker", "Bot", "Klasik"
        ),
        "Aksesuar" to listOf(
            "Kemer", "Kravat", "Şapka", "Atkı", "Eldiven", "Saat", "Cüzdan"
        )
    )
    
    val bedenler = listOf("XS", "S", "M", "L", "XL", "XXL")
    val renkler = listOf(
        "Siyah", "Beyaz", "Lacivert", "Gri", "Kırmızı", 
        "Mavi", "Yeşil", "Kahverengi"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Filtreler",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Kategori filtreleri
        categories.forEach { (category, subcategories) ->
            FilterCategory(
                title = category,
                items = subcategories
            )
        }
        
        // Fiyat aralığı filtresi
        Text(
            text = "Fiyat Aralığı",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            TextButton(
                onClick = { /* Filtreleri uygula */ },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Primary
                )
            ) {
                Text("Uygula")
            }
        }
        
        // Beden filtresi
        Text(
            text = "Beden",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        ChipGroup(items = bedenler)
        
        // Renk filtresi
        Text(
            text = "Renk",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        ChipGroup(items = renkler)
    }
} 