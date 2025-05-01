package com.example.duan.Model.model

data class Payment(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val method: String = "",
    val status: String = "",
    val transactionId: String = "",
    val createdAt: String = ""
)
