package com.example.duan

import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.duan.Model.api.capturePayPalPayment
import com.example.duan.Model.config.CloudinaryConfig
import com.example.duan.View.navigation.AppNavigation
import com.example.duan.View.theme.DuanTheme
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val moMoResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Payment result - resultCode: ${result.resultCode}, data: ${result.data}")
        if (result.resultCode == RESULT_OK && result.data != null) {
            val uri = result.data?.data
            Log.d(TAG, "URI received: $uri")
            processPaymentResult(uri)
        } else {
            Log.d(TAG, "Payment failed, resultCode: ${result.resultCode}, data: ${result.data}")
            Toast.makeText(this, "Thanh toán bị hủy hoặc thất bại", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            CloudinaryConfig.initCloudinary(this)
            Log.d(TAG, "Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cloudinary: ${e.message}")
        }

        // Kiểm tra intent ban đầu nếu có
        if (intent?.data != null) {
            processPaymentResult(intent?.data)
        }

        setContent {
            DuanTheme {
                val authViewModel: AuthViewModel = viewModel()
                AppNavigation(
                    authViewModel = authViewModel,
                    moMoResultLauncher = moMoResultLauncher,
                    initialIntent = intent
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called with intent: $intent")
        processPaymentResult(intent.data)
    }

    /**
     * Xử lý kết quả thanh toán từ URI được trả về
     */
    private fun processPaymentResult(uri: Uri?) {
        uri?.let {
            Log.d(TAG, "Processing payment result URI: $uri")
            if (uri.scheme == "com.example.duan" && uri.host == "paypal") {
                val action = uri.path?.substring(1)
                when (action) {
                    "return" -> {
                        // Lấy orderId từ URL parameter
                        val orderId = uri.getQueryParameter("token")
                        if (orderId != null) {
                            Log.d(TAG, "PayPal payment approved, order ID: $orderId")
                            completePayPalPayment(orderId)
                        } else {
                            Log.e(TAG, "PayPal return URI missing token parameter")
                            Toast.makeText(this, "Lỗi: Thiếu mã đơn hàng PayPal", Toast.LENGTH_LONG).show()
                        }
                    }
                    "cancel" -> {
                        Log.d(TAG, "PayPal payment cancelled")
                        Toast.makeText(this, "Thanh toán PayPal bị hủy", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Log.d(TAG, "Unknown PayPal action: $action")
                        Toast.makeText(this, "Hành động PayPal không xác định", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /**
     * Hoàn tất thanh toán PayPal bằng cách capture payment
     */
    private fun completePayPalPayment(orderId: String) {
        // Hiển thị thông báo đang xử lý
        Toast.makeText(this, "Đang xử lý thanh toán PayPal...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val captureResponse = capturePayPalPayment(orderId)
                if (captureResponse != null && captureResponse.status == "COMPLETED") {
                    Log.d(TAG, "PayPal payment captured successfully: ${captureResponse.id}")

                    // Cập nhật UI và database sau khi capture thành công
                    Toast.makeText(this@MainActivity, "Thanh toán PayPal thành công!", Toast.LENGTH_LONG).show()

                    // TODO: Cập nhật trạng thái đơn hàng trong database
                    // updateOrderStatus(orderId, "PAID")
                } else {
                    Log.e(TAG, "PayPal capture failed or returned unexpected status")
                    Toast.makeText(this@MainActivity, "Xác nhận thanh toán PayPal thất bại", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing PayPal payment: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Lỗi xác nhận thanh toán: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}