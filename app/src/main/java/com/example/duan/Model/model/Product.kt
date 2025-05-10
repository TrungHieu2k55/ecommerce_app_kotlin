package com.example.duan.Model.model

import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    var quantityInStock: Int = 0, // Sử dụng var để có thể gán lại
    val category: String = "",
    val images: List<String> = emptyList(),
    val createdAt: String = "",
    val rating: Double = 0.0,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val brand: String? = null,
    val gender: String? = null,
    val quantitySold: Int = 0,
    val addedToCartCount: Int = 0,
    val views: Int = 0
) {


    fun getCreatedAtDate(): java.util.Date? {
        return try {
            val formatters = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )
            formatters.firstNotNullOfOrNull { formatter ->
                try { formatter.parse(createdAt) } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            null
        }
    }
}