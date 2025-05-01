package com.example.duan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.duan.Model.config.CloudinaryConfig
import com.example.duan.View.navigation.AppNavigation
import com.example.duan.View.theme.DuanTheme
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryConfig.initCloudinary(this) // Khởi tạo Cloudinary
        setContent {
            DuanTheme {
                val authViewModel: AuthViewModel = viewModel()
                AppNavigation(authViewModel)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppNavigationPreview() {
    DuanTheme {
        // Không thể tạo AuthViewModel trong preview vì nó cần Hilt và runtime context
        // Thay vào đó, hiển thị thông báo hoặc mock dữ liệu nếu cần
        Text("Preview not available due to ViewModel dependency")
    }
}