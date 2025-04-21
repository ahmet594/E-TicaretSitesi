package com.example.mobilsmartwear.di

import android.content.Context
import com.example.mobilsmartwear.data.repository.AuthRepository
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.FavoriteRepository
import com.example.mobilsmartwear.data.repository.ProductRepository

/**
 * Dependency Injection için basit bir modül
 * Daha karmaşık ihtiyaçlar için Dagger/Hilt kullanılabilir
 */
object AppModule {
    
    /**
     * ProductRepository instance'ı sağlar
     */
    fun provideProductRepository(context: Context): ProductRepository {
        return ProductRepository()
    }
    
    /**
     * AuthRepository instance'ı sağlar
     */
    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(context)
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