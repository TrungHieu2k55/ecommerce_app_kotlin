package com.example.duan.Model.model

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    var totalPrice: Double = 0.0,
    val status: String = "Pending",
    val tracking: TrackingInfo? = null,
    val createdAt: Timestamp? = null,
    val shippingAddress: String = "",
    val couponCode: String? = null,
    val discount: Double = 0.0
)

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)

data class TrackingInfo(
    val carrier: String = "",
    val trackingNumber: String = "",
    val estimatedDelivery: String = ""
)