package com.example.duan.View.home

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.duan.Model.api.createPayPalPayment
import com.example.duan.Model.api.capturePayPalPayment
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import com.example.duan.Model.model.Order
import com.example.duan.Model.model.OrderItem
import com.example.duan.Model.repository.FirestoreRepository
import com.example.duan.ViewModel.cart.CartViewModel
import com.example.duan.ViewModel.OrderViewModel.OrderViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userId: String,
    totalCost: Double,
    moMoResultLauncher: ActivityResultLauncher<Intent>,
    orderId: String? = null
) {
    val paymentMethods = authViewModel.userProfile.value?.paymentMethods ?: emptyList()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    var paymentJob by remember { mutableStateOf<Job?>(null) }
    var isPayPalPaymentInitiated by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val cartViewModel: CartViewModel = hiltViewModel()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartError by cartViewModel.error.collectAsState()
    val orderViewModel: OrderViewModel = hiltViewModel()

    val userProfile by authViewModel.userProfile.collectAsState()
    val selectedAddress = remember(userProfile) {
        userProfile?.addresses?.find { it.title == userProfile?.selectedAddress }?.details
            ?: "Chưa có địa chỉ được chọn"
    }
    val phoneNumber = userProfile?.phoneNumber ?: "Không có thông tin"

    LaunchedEffect(userId) {
        if (cartItems.isEmpty()) {
            Log.d("PaymentMethodsScreen", "Khởi tạo CartViewModel cho userId: $userId")
            cartViewModel.init(userId)
        }
    }

    LaunchedEffect(cartError) {
        cartError?.let { error ->
            cartViewModel.removeInvalidItems()
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Chỉnh sửa giỏ hàng",
                duration = SnackbarDuration.Long
            ).let { result ->
                if (result == SnackbarResult.ActionPerformed) {
                    navController.navigate("cart")
                }
            }
        }
    }

    LaunchedEffect(orderId) {
        if (orderId != null && isPayPalPaymentInitiated) {
            coroutineScope.launch {
                try {
                    isProcessing = true
                    Log.d("PaymentMethodsScreen", "Xử lý deep link PayPal với orderId: $orderId")
                    val captureResponse = capturePayPalPayment(orderId)
                    if (captureResponse?.status == "COMPLETED") {
                        Log.d("PaymentMethodsScreen", "Thanh toán PayPal thành công, kiểm tra giỏ hàng")
                        if (cartViewModel.validateCartItems()) {
                            val order = Order(
                                orderId = orderId,
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
                                createdAt = com.google.firebase.Timestamp.now(),
                                shippingAddress = "$selectedAddress | Số điện thoại: $phoneNumber",
                                couponCode = null,
                                discount = 0.0
                            )
                            Log.d("PaymentMethodsScreen", "Cart items before saving order: $cartItems")
                            FirestoreRepository().saveOrder(order)
                            cartViewModel.clearCart()
                            orderViewModel.refreshOrdersAfterPayment()
                            if (!hasNavigated) {
                                Log.d("PaymentMethodsScreen", "Điều hướng đến payment_success với orderId: $orderId")
                                navController.navigate("payment_success/$userId/$orderId/$totalCost")
                                hasNavigated = true
                            }
                        } else {
                            Log.e("PaymentMethodsScreen", "Giỏ hàng không hợp lệ: ${cartError}")
                            Toast.makeText(context, cartError ?: "Giỏ hàng không hợp lệ", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("PaymentMethodsScreen", "Thanh toán PayPal không hoàn tất: ${captureResponse?.status}")
                        Toast.makeText(context, "Thanh toán PayPal không hoàn tất", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("PaymentMethodsScreen", "Lỗi khi capture PayPal: ${e.message}", e)
                    Toast.makeText(context, "Lỗi khi xử lý thanh toán PayPal: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isProcessing = false
                    isPayPalPaymentInitiated = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phương thức thanh toán") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F5F5),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (paymentMethods.isNotEmpty()) {
                Text(
                    text = "Phương thức thanh toán đã liên kết",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(paymentMethods) { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    selectedPaymentMethod = method
                                    isPayPalPaymentInitiated = false
                                }
                                .background(
                                    if (selectedPaymentMethod == method) Color(0xFFE3F2FD) else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(method, fontSize = 16.sp)
                            IconButton(onClick = {
                                val updatedMethods = paymentMethods.toMutableList().apply { remove(method) }
                                authViewModel.updatePaymentMethods(updatedMethods)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa")
                            }
                        }
                        Divider()
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tùy chọn thanh toán",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                PaymentOptionItem(
                    icon = Icons.Default.CreditCard,
                    name = "Thẻ tín dụng/Thẻ ghi nợ",
                    isSelected = selectedPaymentMethod == "BankCard",
                    onClick = {
                        selectedPaymentMethod = "BankCard"
                        isPayPalPaymentInitiated = false
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                PaymentOptionItem(
                    icon = Icons.Default.Money,
                    name = "Thanh toán khi nhận hàng (COD)",
                    isSelected = selectedPaymentMethod == "COD",
                    onClick = {
                        selectedPaymentMethod = "COD"
                        isPayPalPaymentInitiated = false
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                PaymentOptionItem(
                    icon = Icons.Default.Phone,
                    name = "PayPal",
                    isSelected = selectedPaymentMethod == "PayPal",
                    onLinkClick = {
                        if (totalCost <= 0) {
                            Toast.makeText(context, "Số tiền phải lớn hơn 0", Toast.LENGTH_LONG).show()
                        } else if (cartItems.isEmpty()) {
                            Toast.makeText(context, "Giỏ hàng trống, không thể tạo đơn hàng", Toast.LENGTH_LONG).show()
                        } else {
                            coroutineScope.launch {
                                if (cartViewModel.validateCartItems()) {
                                    try {
                                        isPayPalPaymentInitiated = true
                                        selectedPaymentMethod = "PayPal"
                                        val approvalUrlFromApi = createPayPalPayment(userId, totalCost)
                                        if (approvalUrlFromApi.isNullOrEmpty()) {
                                            Log.e("PaymentMethodsScreen", "URL phê duyệt rỗng hoặc null")
                                            Toast.makeText(context, "Không thể tạo URL thanh toán PayPal", Toast.LENGTH_LONG).show()
                                            isPayPalPaymentInitiated = false
                                            return@launch
                                        }
                                        Log.d("PayPal", "URL phê duyệt: $approvalUrlFromApi")
                                        val customTabsIntent = CustomTabsIntent.Builder().build()
                                        customTabsIntent.launchUrl(context, Uri.parse(approvalUrlFromApi))
                                    } catch (e: Exception) {
                                        Log.e("PaymentMethodsScreen", "Thanh toán PayPal thất bại: ${e.toString()}", e)
                                        Toast.makeText(context, "Thanh toán PayPal thất bại: ${e.message}", Toast.LENGTH_LONG).show()
                                        isPayPalPaymentInitiated = false
                                    }
                                } else {
                                    Toast.makeText(context, cartError ?: "Giỏ hàng không hợp lệ", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedPaymentMethod == "PayPal" && isPayPalPaymentInitiated) {
                        Toast.makeText(context, "Đang xử lý thanh toán PayPal, vui lòng hoàn tất quá trình thanh toán", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (selectedPaymentMethod != null && !isProcessing) {
                        isProcessing = true
                        paymentJob?.cancel()
                        paymentJob = coroutineScope.launch {
                            if (cartItems.isEmpty()) {
                                Toast.makeText(context, "Giỏ hàng trống, không thể tạo đơn hàng", Toast.LENGTH_LONG).show()
                                isProcessing = false
                                return@launch
                            }

                            if (!cartViewModel.validateCartItems()) {
                                Toast.makeText(context, cartError ?: "Giỏ hàng không hợp lệ", Toast.LENGTH_LONG).show()
                                isProcessing = false
                                return@launch
                            }

                            try {
                                if (selectedPaymentMethod != "PayPal") {
                                    val paymentId = if (selectedPaymentMethod == "COD") "cod_${UUID.randomUUID()}" else "card_${UUID.randomUUID()}"
                                    val order = Order(
                                        orderId = paymentId,
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
                                        status = if (selectedPaymentMethod == "COD") "Processing" else "Paid",
                                        createdAt = com.google.firebase.Timestamp.now(),
                                        shippingAddress = "$selectedAddress | Số điện thoại: $phoneNumber",
                                        couponCode = null,
                                        discount = 0.0
                                    )

                                    Log.d("PaymentMethodsScreen", "Chuẩn bị lưu đơn hàng với orderId: $paymentId, cartItems: $cartItems")
                                    FirestoreRepository().saveOrder(order)
                                    Log.d("PaymentMethodsScreen", "Đã lưu đơn hàng với orderId: $paymentId")
                                    cartViewModel.clearCart()
                                    orderViewModel.refreshOrdersAfterPayment()

                                    if (!hasNavigated) {
                                        Log.d("PaymentMethodsScreen", "Điều hướng đến payment_success với orderId: $paymentId")
                                        navController.navigate("payment_success/$userId/$paymentId/$totalCost")
                                        hasNavigated = true
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("PaymentMethodsScreen", "Lỗi khi lưu đơn hàng: ${e.message}", e)
                                snackbarHostState.showSnackbar(
                                    message = e.message ?: "Lỗi khi tạo đơn hàng",
                                    actionLabel = "Chỉnh sửa giỏ hàng",
                                    duration = SnackbarDuration.Long
                                ).let { result ->
                                    if (result == SnackbarResult.ActionPerformed) {
                                        navController.navigate("cart")
                                    }
                                }
                            } finally {
                                isProcessing = false
                                paymentJob = null
                            }
                        }
                    } else if (selectedPaymentMethod == null) {
                        Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isProcessing) Color.Gray else Color(0xFF1E88E5),
                    contentColor = Color.White
                ),
                enabled = !isProcessing
            ) {
                Text(
                    text = if (isProcessing) "Đang xử lý..." else "Xác nhận thanh toán",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PaymentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLinkClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { if (name == "PayPal") onLinkClick() else onClick() }
            .background(
                if (isSelected) Color(0xFFE3F2FD) else Color.White
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = Color(0xFF666666),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
        Text(
            text = if (name == "PayPal") "Liên kết" else if (isSelected) "Đã chọn" else "",
            color = Color(0xFF1E88E5),
            fontSize = 14.sp
        )
    }
}