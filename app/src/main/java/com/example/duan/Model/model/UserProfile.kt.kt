package com.example.duan.Model.model

data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val addresses: List<Address> = emptyList(), // Danh sách địa chỉ
    val selectedAddress: String? = null, // Tiêu đề của địa chỉ được chọn
    val paymentMethods: List<String> = emptyList()
)

data class Address(
    val title: String = "",
    val details: String = ""
)