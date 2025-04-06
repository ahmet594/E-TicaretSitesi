package com.example.mobilsmartwear.di

import android.content.Context
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.FavoriteRepository
import com.example.mobilsmartwear.data.repository.ProductRepository

/**
 * Bağımlılık enjeksiyonu için modül
 */
object AppModule {
    
    // Repository Providers
    fun provideProductRepository(context: Context): ProductRepository {
        return ProductRepository()
    }
    
    fun provideCartRepository(context: Context): CartRepository {
        val productRepository = provideProductRepository(context)
        return CartRepository(productRepository)
    }
    
    fun provideFavoriteRepository(context: Context): FavoriteRepository {
        val productRepository = provideProductRepository(context)
        return FavoriteRepository(productRepository)
    }
} 