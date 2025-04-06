package com.example.mobilsmartwear.ui.screens.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.ui.components.CartItemRow

@Composable
fun CartScreen(
    onNavigateToCheckout: () -> Unit,
    cartViewModel: CartViewModel = viewModel()
) {
    var cartItems by remember { mutableStateOf<List<Pair<CartItem, Product?>>>(emptyList()) }
    var totalAmount by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(Unit) {
        cartViewModel.cartItems.collect { items ->
            cartItems = items
            totalAmount = items.sumOf { (cartItem, product) -> 
                (product?.price ?: 0.0) * cartItem.quantity 
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sepet başlığı
        Text(
            text = "Sepetim",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (cartItems.isEmpty()) {
            // Boş sepet mesajı
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sepetiniz boş",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Sepet ürünleri listesi
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { (cartItem, product) ->
                    CartItemRow(
                        cartItem = cartItem,
                        product = product,
                        onQuantityIncrease = { cartViewModel.increaseQuantity(cartItem) },
                        onQuantityDecrease = { cartViewModel.decreaseQuantity(cartItem) },
                        onRemove = { cartViewModel.removeFromCart(cartItem) }
                    )
                }
            }
            
            // Toplam tutar ve ödeme butonu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Toplam Tutar:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "₺${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Button(
                    onClick = onNavigateToCheckout,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cartItems.isNotEmpty()
                ) {
                    Text("Ödemeye Geç")
                }
            }
        }
    }
} 