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
                firestore.collection("products").get().await()
            } else {
                firestore.collection("products")
                    .orderBy("name")
                    .startAt(query.lowercase())
                    .endAt(query.lowercase() + "\uf8ff")
                    .get()
                    .await()
            }
            val products = snapshot.documents.mapNotNull { document ->
                document.toObject<Product>()
            }
            println("Search products: ${products.size} found for query=$query")
            products
        } catch (e: Exception) {
            println("Error searching products: ${e.message}")
            throw e
        }
    }

    suspend fun filterProducts(
        brand: String? = null,
        gender: String? = null,
        sortBy: String? = null,
        priceRange: Pair<Double, Double>? = null,
        minRating: Double? = null,
        maxRating: Double? = null // Thêm giới hạn trên
    ): List<Product> {
        return try {
            var query: Query = firestore.collection("products")
            println("Filter params: brand=$brand, gender=$gender, sortBy=$sortBy, priceRange=$priceRange, minRating=$minRating, maxRating=$maxRating")

            // Lọc theo brand
            if (brand != null && brand != "ALL") {
                query = query.whereEqualTo("brand", brand)
            }

            // Lọc theo gender
            if (gender != null && gender != "ALL") {
                query = query.whereEqualTo("gender", gender)
            }

            // Lấy dữ liệu từ Firestore
            val snapshot = query.get().await()
            var products = snapshot.documents.mapNotNull { document ->
                document.toObject<Product>()
            }

            // Lọc rating trong bộ nhớ
            if (minRating != null) {
                products = products.filter { it.rating >= minRating }
            }
            if (maxRating != null) {
                products = products.filter { it.rating <= maxRating }
            }

            // Lọc price trong bộ nhớ
            if (priceRange != null) {
                products = products.filter { it.price in priceRange.first..priceRange.second }
            }

            // Sắp xếp trong bộ nhớ
            if (sortBy != null) {
                products = when (sortBy) {
                    "Price High" -> products.sortedByDescending { it.price }
                    "Popular" -> products.sortedByDescending { it.rating }
                    "Most Recent" -> products.sortedByDescending { it.createdAt }
                    else -> products
                }
            }

            println("Filtered products: ${products.size} found")
            products
        } catch (e: Exception) {
            println("Error filtering products: ${e.message}")
            throw e
        }
    }
}