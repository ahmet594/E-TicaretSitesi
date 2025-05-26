package com.example.mobilsmartwear.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilsmartwear.data.model.Product
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale

private const val TAG = "ProductCard"

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
    isFavorite: Boolean = false,
    showFavoriteButton: Boolean = false,
    isCompact: Boolean = false,
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {},
    showAddToCartButton: Boolean = false,
    onAddToCartClick: () -> Unit = {},
    isProblematic: Boolean = false
) {
    val context = LocalContext.current
    Log.d(TAG, "Rendering product: ${product.id}, ${product.name}, Image URL: ${product.imageUrl}")
    
    Card(
        modifier = modifier
            .width(if (isCompact) 120.dp else 160.dp)
            .height(
                if (isCompact) {
                    if (showAddToCartButton) 220.dp else 180.dp
                } else {
                    240.dp
                }
            )
            .clickable { onClick() }
            .then(
                if (isProblematic) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Ürün resmi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 100.dp else 120.dp)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .build(),
                    contentDescription = product.name,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 100.dp else 120.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(if (isCompact) 24.dp else 32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 100.dp else 120.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(if (isCompact) 16.dp else 24.dp)
                                )
                                if (!isCompact) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Resim yüklenemedi",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Log.e(TAG, "Failed to load image: ${product.imageUrl}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompact) 100.dp else 120.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Favori butonu sadece FavoritesScreen'de gösterilecek
                if (showFavoriteButton) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                            tint = if (isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // Remove butonu seçili ürünlerde gösterilecek
                if (showRemoveButton) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onRemoveClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kaldır",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                
                // Stok durumu
                if (!product.hasStock()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stokta Yok",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Ürün bilgileri
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 8.dp else 12.dp)
            ) {
                Text(
                    text = product.name,
                    style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall,
                    maxLines = if (isCompact) 2 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(if (isCompact) 28.dp else 40.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${product.price} TL",
                    style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Sepete ekle butonu
                if (showAddToCartButton) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onAddToCartClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = "Sepete Ekle",
                            fontSize = if (isCompact) 12.sp else 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Compact modda marka ve etiketleri gösterme
                if (!isCompact) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (product.featured) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Öne Çıkan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}