package com.example.duan.Model.model

import java.text.SimpleDateFormat
import java.util.Locale

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val category: String = "",
    val images: List<String> = emptyList(),
    val createdAt: String = "",
    val rating: Double = 0.0,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val brand: String? = null,
    val gender: String? = null,
    val quantitySold: Int = 0, // Thêm: Số lượng đã bán
    val addedToCartCount: Int = 0 // Thêm: Số lần được thêm vào giỏ hàng
) {
    fun getCreatedAtDate(): java.util.Date? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            formatter.parse(createdAt)
        } catch (e: Exception) {
            null
        }
    }
}