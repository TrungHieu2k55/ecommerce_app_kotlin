package com.example.duan.Model.api

import com.example.duan.Model.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

object ProductApiClient {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun searchProducts(query: String): List<Product> {
        return try {
            val snapshot = if (query.isBlank()) {
                // Nếu không có query, lấy tất cả sản phẩm
                firestore.collection("products")
                    .get()
                    .await()
            } else {
                // Tìm kiếm sản phẩm theo tên (gần đúng)
                firestore.collection("products")
                    .orderBy("name")
                    .startAt(query.lowercase())
                    .endAt(query.lowercase() + "\uf8ff")
                    .get()
                    .await()
            }

            snapshot.documents.mapNotNull { document ->
                document.toObject<Product>()
            }
        } catch (e: Exception) {
            println("Error searching products: ${e.message}")
            emptyList()
        }
    }

    suspend fun filterProducts(
        brand: String? = null,
        gender: String? = null,
        sortBy: String? = null,
        priceRange: Pair<Double, Double>? = null,
        minRating: Double? = null
    ): List<Product> {
        return try {
            // Bắt đầu với truy vấn cơ bản, khai báo kiểu là Query
            var query: Query = firestore.collection("products")

            // Lọc theo brand
            if (brand != null && brand != "ALL") {
                query = query.whereEqualTo("brand", brand)
            }

            // Lọc theo gender
            if (gender != null && gender != "ALL") {
                query = query.whereEqualTo("gender", gender)
            }

            // Lọc theo rating
            if (minRating != null) {
                query = query.whereGreaterThanOrEqualTo("rating", minRating)
            }

            // Lọc theo giá
            if (priceRange != null) {
                query = query.whereGreaterThanOrEqualTo("price", priceRange.first)
                    .whereLessThanOrEqualTo("price", priceRange.second)
            }

            // Sắp xếp
            if (sortBy != null) {
                when (sortBy) {
                    "Price High" -> query = query.orderBy("price", Query.Direction.DESCENDING)
                    "Popular" -> query = query.orderBy("rating", Query.Direction.DESCENDING)
                    "Most Recent" -> query = query // Nếu có trường timestamp, bạn có thể thêm orderBy("timestamp")
                }
            }

            // Lấy dữ liệu
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject<Product>()
            }
        } catch (e: Exception) {
            println("Error filtering products: ${e.message}")
            emptyList()
        }
    }
}