package com.example.duan.Model.model

data class Recommendation(
    val userId: String = "",
    val viewed: List<String> = emptyList(),
    val bought: List<String> = emptyList(),
    val recommended: List<String> = emptyList()
)
