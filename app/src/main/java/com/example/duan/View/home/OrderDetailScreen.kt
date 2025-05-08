package com.example.duan.View.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.Order
import com.example.duan.ViewModel.OrderViewModel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    orderId: String,
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val orders by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val error by orderViewModel.error.collectAsState()
    val order = orders.find { it.orderId == orderId }

    // Log giá trị orderId khi vào màn hình và thử lấy lại dữ liệu nếu không tìm thấy đơn hàng
    LaunchedEffect(Unit) {
        Log.d("OrderDetailScreen", "orderId received: $orderId")
        repeat(5) { // Thử tối đa 5 lần
            orderViewModel.fetchOrders()
            if (orders.any { it.orderId == orderId }) {
                Log.d("OrderDetailScreen", "Order found after retry: $orderId")
                return@LaunchedEffect
            }
            Log.d("OrderDetailScreen", "Order not found, retrying... Attempt: ${it + 1}")
            delay(2000) // Chờ 2 giây trước khi thử lại
        }
        Log.d("OrderDetailScreen", "Order not found after 5 retries, fetching directly: $orderId")
        // Thử lấy trực tiếp đơn hàng từ Firestore
        orderViewModel.fetchOrderById(orderId)
    }

    // Log trạng thái của orders, isLoading, và error mỗi khi chúng thay đổi
    LaunchedEffect(orders, isLoading, error) {
        Log.d("OrderDetailScreen", "orders: $orders")
        Log.d("OrderDetailScreen", "isLoading: $isLoading")
        Log.d("OrderDetailScreen", "error: $error")
        Log.d("OrderDetailScreen", "order found: $order")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết đơn hàng #${orderId.takeLast(8)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
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
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Log.d("OrderDetailScreen", "Showing loading state")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4FC3F7))
                }
            } else if (error != null) {
                Log.d("OrderDetailScreen", "Showing error state: $error")
                if (error == "Please log in to view your orders") {
                    LaunchedEffect(Unit) {
                        navController.navigate("login_screen") // Điều hướng đến màn hình đăng nhập
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Đã xảy ra lỗi",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            } else if (order != null) {
                Log.d("OrderDetailScreen", "Showing order details for order: $order")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Order status timeline
                    OrderStatusTimeline(order)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Order items
                    OrderItemsSection(order)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Shipping details
                    ShippingDetailsSection(order)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Total price
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tổng cộng",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$${String.format("%.2f", order.totalPrice.toDouble())}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4FC3F7)
                            )
                        }
                    }
                }
            } else {
                Log.d("OrderDetailScreen", "Showing 'Không tìm thấy đơn hàng' state")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy đơn hàng",
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusTimeline(order: Order) {
    val statusList = listOf(
        "Delivered" to (order.status == "Delivered"),
        "Shipped" to (order.status == "Shipped" || order.status == "Delivered"),
        "Order Confirmed" to (order.status == "Processing" || order.status == "Shipped" || order.status == "Delivered"),
        "Order Placed" to true
    )
    val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    val orderDate = order.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Unknown"

    Column(modifier = Modifier.fillMaxWidth()) {
        statusList.forEach { (status, isActive) ->
            StatusTimelineItem(
                status = status,
                isActive = isActive,
                date = orderDate
            )
        }
    }
}

@Composable
fun StatusTimelineItem(status: String, isActive: Boolean, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline circle indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color(0xFF4FC3F7)
                    else Color(0xFFEEEEEE)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (status == "Delivered" && isActive) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Text(
                text = date,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OrderItemsSection(order: Order) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sản phẩm trong đơn hàng",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${order.items.size} Sản phẩm",
                color = Color(0xFF4FC3F7),
                fontSize = 12.sp
            )
        }

        LazyColumn {
            items(order.items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hình ảnh sản phẩm
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Thông tin sản phẩm
                        Column {
                            Text(
                                text = item.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Số lượng: ${item.quantity}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${String.format("%.2f", item.price.toDouble())}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4FC3F7)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShippingDetailsSection(order: Order) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Thông tin giao hàng",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = order.shippingAddress,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Số điện thoại: Không có thông tin",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}