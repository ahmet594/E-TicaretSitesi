package com.example.mobilsmartwear.ui.screens.category

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    category: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModel.provideFactory(category)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Filtreleme ve sıralama için state'ler
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var selectedPriceRange by remember { mutableStateOf<PriceRange?>(null) }
    var selectedSortOption by remember { mutableStateOf(SortOption.RECOMMENDED) }
    var selectedFilters by remember { mutableStateOf(emptyList<String>()) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(56.dp)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )
                
                // Yatay olarak ekranı kaplayan yan yana filtrele ve sırala butonları
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sıralama butonu
                    Button(
                        onClick = { showSortDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sırala",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Filtreleme butonu
                    Button(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Filtrele",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Aktif filtreler ve sıralama seçeneği gösterimi
                if ((category == "Giyim" || category == "Ayakkabı" || category == "Aksesuar") && (selectedFilters.isNotEmpty() || selectedPriceRange != null || selectedSortOption != SortOption.RECOMMENDED)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedSortOption != SortOption.RECOMMENDED) {
                            FilterChip(
                                selected = true,
                                onClick = { showSortDialog = true },
                                label = { 
                                    Text(
                                        text = selectedSortOption.title,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) 
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier.height(42.dp)
                            )
                        }
                        
                        selectedPriceRange?.let {
                            FilterChip(
                                selected = true,
                                onClick = { showFilterDialog = true },
                                label = { 
                                    Text(
                                        text = "${it.min} TL - ${it.max} TL",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) 
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier.height(42.dp)
                            )
                        }
                        
                        selectedFilters.forEach { filter ->
                            FilterChip(
                                selected = true,
                                onClick = { 
                                    selectedFilters = selectedFilters.filter { it != filter }
                                    viewModel.applyFilters(selectedFilters, selectedPriceRange, selectedSortOption)
                                },
                                label = { 
                                    Text(
                                        text = filter,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) 
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier.height(42.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.error ?: "Bir hata oluştu")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.products) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductClick(product.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "${product.price} ₺",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Sıralama Dialog
        if (showSortDialog) {
            AlertDialog(
                onDismissRequest = { showSortDialog = false },
                title = { Text("Sıralama Seçenekleri", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        SortOption.values().forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSortOption = option
                                        viewModel.applyFilters(selectedFilters, selectedPriceRange, option)
                                        showSortDialog = false
                                    }
                                    .padding(vertical = 16.dp), // Daha fazla tıklama alanı
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSortOption == option,
                                    onClick = {
                                        selectedSortOption = option
                                        viewModel.applyFilters(selectedFilters, selectedPriceRange, option)
                                        showSortDialog = false
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = option.title, fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSortDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Kapat", fontSize = 16.sp)
                    }
                }
            )
        }
        
        // Filtreleme Dialog
        if (showFilterDialog && (category == "Giyim" || category == "Ayakkabı" || category == "Aksesuar")) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("Filtrele", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Fiyat Aralığı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // İki sütunlu tablo şeklinde fiyat aralıklarını göster
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FilterChip(
                                    selected = selectedPriceRange == PriceRange(0, 100),
                                    onClick = {
                                        val range = PriceRange(0, 100)
                                        selectedPriceRange = if (selectedPriceRange == range) null else range
                                    },
                                    label = { 
                                        Text(
                                            text = "0 - 100 TL",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(4.dp)
                                        ) 
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .padding(end = 4.dp)
                                )
                                
                                FilterChip(
                                    selected = selectedPriceRange == PriceRange(100, 250),
                                    onClick = { 
                                        val range = PriceRange(100, 250)
                                        selectedPriceRange = if (selectedPriceRange == range) null else range
                                    },
                                    label = { 
                                        Text(
                                            text = "100 - 250 TL",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(4.dp)
                                        ) 
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .padding(start = 4.dp)
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FilterChip(
                                    selected = selectedPriceRange == PriceRange(250, 500),
                                    onClick = { 
                                        val range = PriceRange(250, 500)
                                        selectedPriceRange = if (selectedPriceRange == range) null else range
                                    },
                                    label = { 
                                        Text(
                                            text = "250 - 500 TL",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(4.dp)
                                        ) 
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .padding(end = 4.dp)
                                )
                                
                                FilterChip(
                                    selected = selectedPriceRange == PriceRange(500, 1000),
                                    onClick = { 
                                        val range = PriceRange(500, 1000)
                                        selectedPriceRange = if (selectedPriceRange == range) null else range
                                    },
                                    label = { 
                                        Text(
                                            text = "500 - 1000 TL",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(4.dp)
                                        ) 
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .padding(start = 4.dp)
                                )
                            }
                            
                            FilterChip(
                                selected = selectedPriceRange == PriceRange(1000, Int.MAX_VALUE, "1000+ TL"),
                                onClick = { 
                                    val range = PriceRange(1000, Int.MAX_VALUE, "1000+ TL")
                                    selectedPriceRange = if (selectedPriceRange == range) null else range
                                },
                                label = { 
                                    Text(
                                        text = "1000+ TL",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(4.dp)
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )
                        }
                        
                        HorizontalDivider(thickness = 1.dp)
                        
                        Text(
                            text = "Tür",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        // Filtre listesini scrollable yapmak için LazyColumn kullanımı
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) // Yüksekliği sınırla ki scroll görünür olsun
                        ) {
                            items(
                                when (category) {
                                    "Giyim" -> listOf(
                                        FilterPair("T-Shirt", "t-shirt"),
                                        FilterPair("Gömlek", "gömlek"),
                                        FilterPair("Kazak", "kazak"),
                                        FilterPair("Hırka", "hırka"),
                                        FilterPair("Sweatshirt", "sweatshirt"),
                                        FilterPair("Mont", "mont"),
                                        FilterPair("Kaban & Parka", "kaban-parka"),
                                        FilterPair("Ceket", "ceket"),
                                        FilterPair("Kot Pantolon", "kot-pantolon"),
                                        FilterPair("Kumaş Pantolon", "kumaş-pantolon"),
                                        FilterPair("Şortlar", "şortlar"),
                                        FilterPair("Takım Elbise", "takım-elbise"),
                                        FilterPair("Eşofman", "eşofman"),
                                        FilterPair("Yelek", "yelek")
                                    )
                                    "Ayakkabı" -> listOf(
                                        FilterPair("Spor Ayakkabı", "spor-ayakkabı"),
                                        FilterPair("Sneaker", "sneaker"),
                                        FilterPair("Bot", "bot"),
                                        FilterPair("Klasik", "klasik")
                                    )
                                    "Aksesuar" -> listOf(
                                        FilterPair("Saat", "saat"),
                                        FilterPair("Kemer", "kemer"),
                                        FilterPair("Kravat", "kravat"),
                                        FilterPair("Şapka", "sapka"),
                                        FilterPair("Atkı", "atki"),
                                        FilterPair("Eldiven", "eldiven"),
                                        FilterPair("Cüzdan", "cüzdan")
                                    )
                                    else -> emptyList()
                                }
                            ) { filterPair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedFilters = if (selectedFilters.contains(filterPair.backendValue)) {
                                                selectedFilters - filterPair.backendValue
                                            } else {
                                                selectedFilters + filterPair.backendValue
                                            }
                                        }
                                        .padding(vertical = 12.dp), // Daha büyük tıklama alanı
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedFilters.contains(filterPair.backendValue),
                                        onCheckedChange = { checked ->
                                            selectedFilters = if (checked) {
                                                selectedFilters + filterPair.backendValue
                                            } else {
                                                selectedFilters - filterPair.backendValue
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = filterPair.displayValue, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.applyFilters(selectedFilters, selectedPriceRange, selectedSortOption)
                            showFilterDialog = false 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Filtrele", fontSize = 16.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showFilterDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("İptal", fontSize = 16.sp)
                    }
                }
            )
        }
    }
}

data class PriceRange(
    val min: Int,
    val max: Int,
    val displayText: String = "$min - $max TL"
)

enum class SortOption(val title: String) {
    RECOMMENDED("Önerilen"),
    PRICE_LOW_TO_HIGH("Fiyat: Düşükten Yükseğe"),
    PRICE_HIGH_TO_LOW("Fiyat: Yüksekten Düşüğe"),
    NEWEST("En Yeniler")
}

// Filtre çiftlerini temsil eden veri sınıfı
data class FilterPair(
    val displayValue: String,  // Kullanıcıya gösterilecek değer
    val backendValue: String   // Backend ile eşleşecek değer
) 