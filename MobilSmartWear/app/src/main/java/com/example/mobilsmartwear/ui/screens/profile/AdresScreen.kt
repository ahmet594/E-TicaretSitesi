package com.example.mobilsmartwear.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobilsmartwear.ui.screens.auth.AuthViewModel
import com.example.mobilsmartwear.ui.screens.auth.AddressUpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdresScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val addressUpdateState by viewModel.addressUpdateState.collectAsState()
    
    // Adres düzenleme durumu
    var isEditing by remember { mutableStateOf(false) }
    
    // Mevcut adres ve düzenlenecek adres
    val currentAddress = userState?.address ?: ""
    var editedAddress by remember { mutableStateOf(currentAddress) }
    
    // Kullanıcı state'i değiştiğinde editedAddress'i güncelle
    LaunchedEffect(userState?.address) {
        editedAddress = userState?.address ?: ""
        Log.d("AdresScreen", "Kullanıcı adresi yüklendi: ${userState?.address ?: "Adres yok"}")
    }
    
    // Güncelleme state'i değiştiğinde
    LaunchedEffect(addressUpdateState) {
        when (addressUpdateState) {
            is AddressUpdateState.Success -> {
                Log.d("AdresScreen", "Adres başarıyla güncellendi")
                isEditing = false
            }
            is AddressUpdateState.Error -> {
                Log.e("AdresScreen", "Adres güncellenirken hata: ${(addressUpdateState as AddressUpdateState.Error).message}")
                // Hata durumunda bir şey yapabilirsiniz
            }
            else -> { /* İşlem yok */ }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Adres Bilgilerim",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (!isEditing) {
                        // Düzenleme modunda değilse, düzenleme butonu göster
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle"
                            )
                        }
                    } else {
                        // Düzenleme modundaysa, kaydet butonu göster
                        IconButton(
                            onClick = { 
                                viewModel.updateUserAddress(editedAddress)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Kaydet"
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bilgi ikonu
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Adres",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isEditing) {
                    // Düzenleme modu
                    OutlinedTextField(
                        value = editedAddress,
                        onValueChange = { editedAddress = it },
                        label = { Text("Adres") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // İptal butonu
                    TextButton(
                        onClick = { 
                            isEditing = false
                            editedAddress = currentAddress
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("İptal")
                    }
                } else {
                    // Görüntüleme modu
                    if (currentAddress.isNotEmpty()) {
                        // Adres varsa göster
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = currentAddress,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        // Adres yoksa bilgi mesajı
                        Text(
                            text = "Kayıtlı adres bilginiz bulunmamaktadır.",
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Loading göstergesi
                if (addressUpdateState is AddressUpdateState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                
                // Hata mesajı
                if (addressUpdateState is AddressUpdateState.Error) {
                    Text(
                        text = (addressUpdateState as AddressUpdateState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
} 