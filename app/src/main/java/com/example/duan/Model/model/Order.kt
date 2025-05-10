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
    val inProgressAt: Timestamp? = null,     // Thời gian bắt đầu xử lý
    val shippedAt: Timestamp? = null,        // Thời gian giao hàng
    val deliveredAt: Timestamp? = null,      // Thời gian giao hàng thành công
    val canceledAt: Timestamp? = null,       // Thời gian hủy đơn hàng (nếu có)
    val shippingAddress: String = "",
    val couponCode: String? = null,
    val discount: Double = 0.0,
    var hasReviewed: Boolean = false         // Theo dõi xem đơn hàng đã được đánh giá chưa
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