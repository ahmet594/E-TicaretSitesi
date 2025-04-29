package com.example.mobilsmartwear.di

import android.content.Context
import com.example.mobilsmartwear.data.repository.AuthRepository
import com.example.mobilsmartwear.data.repository.CartRepository
import com.example.mobilsmartwear.data.repository.FavoriteRepository
import com.example.mobilsmartwear.data.repository.OrderRepository
import com.example.mobilsmartwear.data.repository.ProductRepository

/**
 * Dependency Injection için basit bir modül
 * Daha karmaşık ihtiyaçlar için Dagger/Hilt kullanılabilir
 */
object AppModule {
    private var productRepository: ProductRepository? = null
    private var authRepository: AuthRepository? = null
    private var cartRepository: CartRepository? = null
    private var favoriteRepository: FavoriteRepository? = null
    private var orderRepository: OrderRepository? = null
    
    /**
     * ProductRepository instance'ı sağlar
     */
    fun provideProductRepository(context: Context): ProductRepository {
        return productRepository ?: ProductRepository().also {
            productRepository = it
        }
    }
    
    /**
     * AuthRepository instance'ı sağlar
     */
    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: AuthRepository(context).also {
            authRepository = it
        }
    }
    
    fun provideCartRepository(context: Context): CartRepository {
        return cartRepository ?: CartRepository(provideProductRepository(context)).also {
            cartRepository = it
        }
    }
    
    fun provideFavoriteRepository(context: Context): FavoriteRepository {
        return favoriteRepository ?: FavoriteRepository(provideProductRepository(context)).also {
            favoriteRepository = it
        }
    }
    
    fun provideOrderRepository(): OrderRepository {
        return orderRepository ?: OrderRepository().also {
            orderRepository = it
        }
    }
} 