package com.example.duan.Model.model

data class FilterState(
    val brand: String? = null,
    val gender: String? = null,
    val category: String? = null,
    val inStock: Boolean? = null,
    val sortBy: String? = null,
    val priceRange: Pair<Double, Double>? = null,
    val minRating: Double? = null
)