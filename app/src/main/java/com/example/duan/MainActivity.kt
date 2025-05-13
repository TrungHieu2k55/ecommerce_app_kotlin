package com.example.duan

import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.duan.Model.api.capturePayPalPayment
import com.example.duan.Model.config.CloudinaryConfig
import com.example.duan.Model.model.Order
import com.example.duan.Model.model.OrderItem
import com.example.duan.Model.repository.FirestoreRepository
import com.example.duan.View.navigation.AppNavigation
import com.example.duan.View.theme.DuanTheme
import com.example.duan.ViewModel.OrderViewModel.OrderViewModel
import com.example.duan.ViewModel.cart.CartViewModel
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    // Khởi tạo ViewModel bằng lazy để trì hoãn cho đến khi cần
    private val cartViewModel by lazy {
        ViewModelProvider(this).get(CartViewModel::class.java)
    }
    private val orderViewModel by lazy {
        ViewModelProvider(this).get(OrderViewModel::class.java)
    }

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
                // Sử dụng ViewModelProvider thay vì viewModel() để lấy AuthViewModel
                val authViewModel = ViewModelProvider(this@MainActivity).get(AuthViewModel::class.java)
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
                val userId = uri.getQueryParameter("userId") ?: ""
                val totalCost = uri.getQueryParameter("totalCost")?.toDoubleOrNull() ?: 0.0
                when (action) {
                    "return" -> {
                        // Lấy orderId từ URL parameter
                        val orderId = uri.getQueryParameter("token")
                        val generatedOrderId = "paypal_" + UUID.randomUUID().toString()

                        if (orderId != null) {
                            Log.d(TAG, "PayPal payment approved, order ID: $orderId, generated orderId: $generatedOrderId")
                            completePayPalPayment(orderId, userId, totalCost, generatedOrderId)
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
     * Hoàn tất thanh toán PayPal bằng cách capture payment và điều hướng
     */
    private fun completePayPalPayment(payment: String, userId: String, totalCost: Double, generatedOrderId: String) {
        Toast.makeText(this, "Đang xử lý thanh toán PayPal...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val captureResponse = capturePayPalPayment(payment)
                if (captureResponse != null && captureResponse.status == "COMPLETED") {
                    Log.d(TAG, "PayPal payment captured successfully: ${captureResponse.id}")

                    // Lấy danh sách cart items từ cartViewModel
                    val cartItems = cartViewModel.cartItems.value
                    if (cartItems.isEmpty()) {
                        Log.e(TAG, "Giỏ hàng trống, không thể tạo đơn hàng")
                        Toast.makeText(this@MainActivity, "Giỏ hàng trống, không thể tạo đơn hàng", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    val order = Order(
                        orderId = generatedOrderId,
                        userId = userId,
                        items = cartItems.map { cartItem ->
                            OrderItem(
                                productId = cartItem.productId,
                                name = cartItem.productName,
                                imageUrl = cartItem.image,
                                quantity = cartItem.quantity,
                                price = cartItem.price
                            )
                        },
                        totalPrice = totalCost,
                        status = "Paid",
                        createdAt = Timestamp.now().toString(),
                        shippingAddress = "Địa chỉ giao hàng của người dùng",
                        couponCode = null,
                        discount = 0.0
                    )

                    // Lưu đơn hàng vào Firestore
                    FirestoreRepository().saveOrder(order)
                    Log.d(TAG, "Đã lưu đơn hàng với orderId: $generatedOrderId")

                    // Xóa giỏ hàng
                    cartViewModel.clearCart()

                    // Cập nhật danh sách đơn hàng
                    orderViewModel.refreshOrdersAfterPayment()

                    // Hiển thị thông báo thành công
                    Toast.makeText(this@MainActivity, "Thanh toán PayPal thành công!", Toast.LENGTH_LONG).show()

                    // Gửi Intent để điều hướng đến PaymentSuccessScreen
                    val intent = Intent("com.example.duan.PAYMENT_SUCCESS")
                    intent.putExtra("userId", userId)
                    intent.putExtra("paymentId", payment)
                    intent.putExtra("orderId", generatedOrderId)
                    intent.putExtra("totalCost", totalCost)
                    Log.d(TAG, "Sending broadcast with userId=$userId, paymentId=$payment, orderId=$generatedOrderId, totalCost=$totalCost")
                    sendBroadcast(intent)
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