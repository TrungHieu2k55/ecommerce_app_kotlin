package com.example.duan.Model.config

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager

object CloudinaryConfig {
    private var cloudinary: Cloudinary? = null

    fun initCloudinary(context: Context) {
        val config = mapOf<String, Any>(
            "cloud_name" to "dzdytacmj", // Thay bằng cloud_name của bạn
            "api_key" to "373532972882244",       // Thay bằng API Key của bạn
            "api_secret" to "Zjp2-NA4MXxGosBbWw4C80Ef9Kc"  // Thay bằng API Secret của bạn
        )
        try {
            // Khởi tạo MediaManager để hỗ trợ upload
            MediaManager.init(context, config)
            // Tạo instance Cloudinary trực tiếp từ config
            cloudinary = Cloudinary(config)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize Cloudinary: ${e.message}")
        }
    }

    fun getCloudinary(): Cloudinary {
        if (cloudinary == null) {
            throw IllegalStateException("Cloudinary not initialized. Call initCloudinary(context) first.")
        }
        return cloudinary!!
    }
}