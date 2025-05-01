package com.example.duan.Model.model

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val subcategories: List<String>? = null,
    val parent: String? = null
)
