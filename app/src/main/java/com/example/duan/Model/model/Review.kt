package com.example.duan.Model.model

import com.google.firebase.Timestamp

data class Review(
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "", // URL ảnh đại diện của người dùng
    val rating: Float = 0f, // Điểm đánh giá (ví dụ: 4.0)
    val comment: String = "", // Nội dung đánh giá
    val timestamp: Timestamp? = null // Thời gian đăng (ví dụ: "12 days ago")
)

data class Reply(
    val userId: String = "",
    val comment: String = "",
    val createdAt: String = ""
)
