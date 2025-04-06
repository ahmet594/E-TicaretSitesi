package com.example.mobilsmartwear.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Product

@Composable
fun CartItemRow(
    cartItem: CartItem,
    product: Product?,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ürün bilgileri
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product?.name ?: "Ürün bulunamadı",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${product?.price ?: 0.0} TL",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Miktar kontrolleri
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onQuantityDecrease) {
                    Text("-")
                }
                
                Text(
                    text = cartItem.quantity.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                IconButton(onClick = onQuantityIncrease) {
                    Text("+")
                }
                
                IconButton(onClick = onRemove) {
                    Text("X")
                }
            }
        }
    }
} 