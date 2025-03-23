package com.example.mobilsmartwear.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mobilsmartwear.data.model.CartItem
import com.example.mobilsmartwear.data.model.Order
import com.example.mobilsmartwear.data.model.Product
import com.example.mobilsmartwear.data.model.User
import com.example.mobilsmartwear.util.DateConverter
import com.example.mobilsmartwear.util.OrderItemsConverter
import com.example.mobilsmartwear.util.ProductConverters

@Database(
    entities = [Product::class, User::class, CartItem::class, Order::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, OrderItemsConverter::class, ProductConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecommerce_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 