package com.example.mobilsmartwear.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mobilsmartwear.util.DateConverter
import com.example.mobilsmartwear.util.OrderItemsConverter
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
@TypeConverters(DateConverter::class, OrderItemsConverter::class)
data class Order(
    @PrimaryKey
    @SerializedName("_id", alternate = ["id"])
    val id: Int,
    
    @SerializedName("orderNumber")
    val orderNumber: String,
    
    val userId: Int,
    
    @field:TypeConverters(OrderItemsConverter::class)
    val items: List<OrderItem> = emptyList(),
    
    @SerializedName("totalAmount", alternate = ["total"])
    val totalAmount: Double,
    
    val status: String,
    
    @SerializedName("date", alternate = ["createdAt"])
    val date: Long
)

data class OrderItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val productImage: String,
    val price: Double,
    val quantity: Int,
    val size: String? = null,
    val color: String? = null
)

enum class OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
} 