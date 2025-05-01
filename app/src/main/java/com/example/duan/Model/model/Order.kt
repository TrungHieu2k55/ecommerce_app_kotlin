package com.example.duan.Model.model

import com.google.firebase.Timestamp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalCost: Double = 0.0,
    val status: String = "Pending",
    val tracking: TrackingInfo? = null,
    val orderDate: Timestamp? = null // dùng Timestamp để đồng bộ với Firestore
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "", // để hiển thị tên
    val image: String = "",       // để hiển thị ảnh
    val quantity: Int = 0,
    val price: Double = 0.0
)

data class TrackingInfo(
    val carrier: String = "",
    val trackingNumber: String = "",
    val estimatedDelivery: String = ""
)
