package com.example.duan.Model.model

import com.google.firebase.Timestamp

data class Cart(
    val userId: String = "",
    val items: List<CartItem> = emptyList()
)



data class CartItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val size: String = "",
    val color: String = "",
    val image: String = "",
    val addedAt: Timestamp = Timestamp.now()
)
