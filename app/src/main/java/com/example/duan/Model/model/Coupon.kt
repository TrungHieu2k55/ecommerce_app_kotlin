package com.example.duan.Model.model

data class Coupon(
    val id: String = "",
    val code: String = "",
    val discount: Double = 0.0,
    val type: String = "percentage",
    val minOrderValue: Double = 0.0,
    val maxDiscount: Double = 0.0,
    val validFrom: String = "",
    val validTo: String = "",
    val status: String = "active"
)
