package com.example.duan.Model.model

data class ShippingOption(
    val id: String,
    val name: String,
    val estimatedArrival: String,
    val deliveryFee: Double,
    val address: String? = null // Thêm trường địa chỉ cho tùy chọn "Nhà bạn bè"
)