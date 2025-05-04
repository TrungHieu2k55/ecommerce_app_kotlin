package com.example.duan

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp
import android.util.Log

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Thiết lập Facebook App ID & Client Token trước khi khởi tạo SDK
            FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
            FacebookSdk.setClientToken(getString(R.string.facebook_client_token))
            FacebookSdk.sdkInitialize(this)
            AppEventsLogger.activateApp(this)
            Log.d("MyApplication", "Facebook SDK initialized successfully")
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize Facebook SDK: ${e.message}")
        }
    }
}