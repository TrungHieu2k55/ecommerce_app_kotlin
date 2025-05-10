package com.example.duan.View.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

    // Retry logic for fetching order
    LaunchedEffect(Unit) {
        if (order == null) {
            Log.d("OrderDetailScreen", "orderId received: $orderId")
            repeat(3) {
                orderViewModel.fetchOrders()
                if (orders.any { it.orderId == orderId }) {
                    Log.d("OrderDetailScreen", "Order found after retry: $orderId")
                    return@LaunchedEffect
                }
                Log.d("OrderDetailScreen", "Order not found, retrying... Attempt: ${it + 1}")
                delay(1000)
            }
            Log.d("OrderDetailScreen", "Order not found after retries, fetching directly: $orderId")
            orderViewModel.fetchOrderById(orderId)
        }
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4FC3F7))
            }
        } else if (error != null) {
            if (error == "Please log in to view your orders") {
                LaunchedEffect(Unit) {
                    navController.navigate("login_screen")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "Đã xảy ra lỗi",
                    color = Color.Red,
                    fontSize = 16.sp
                )
            }
        } else if (order != null) {
            if (order.items.any { it.imageUrl.isBlank() || it.price < 0 || it.quantity <= 0 } ||
                order.totalPrice < 0 ||
                order.shippingAddress.isBlank()
            ) {
                Log.e("OrderDetailScreen", "Dữ liệu order không hợp lệ: ${order.items}")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dữ liệu đơn hàng không hợp lệ",
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()) // Thay LazyColumn bằng Column + verticalScroll
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
                    Spacer(modifier = Modifier.height(16.dp))

                    // Leave Review button if Delivered and not reviewed
                    if (order.status == "Delivered" && !order.hasReviewed) {
                        Button(
                            onClick = { navController.navigate("leave_review/${order.orderId}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
                        ) {
                            Text(
                                text = "Leave Review",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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

@Composable
fun OrderStatusTimeline(order: Order) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val createdAt = order.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "N/A"
    val inProgressAt = order.inProgressAt?.toDate()?.let { dateFormatter.format(it) } ?: "N/A"
    val shippedAt = order.shippedAt?.toDate()?.let { dateFormatter.format(it) } ?: "N/A"
    val deliveredAt = order.deliveredAt?.toDate()?.let { dateFormatter.format(it) } ?: "N/A"
    val canceledAt = order.canceledAt?.toDate()?.let { dateFormatter.format(it) } ?: "N/A"

    val statusList = listOf(
        Triple("Delivered", order.status == "Delivered", deliveredAt),
        Triple("Shipped", order.status == "Shipped" || order.status == "Delivered", shippedAt),
        Triple(
            "Order Confirmed",
            order.status == "Processing" || order.status == "Shipped" || order.status == "Delivered",
            inProgressAt
        ),
        Triple("Order Placed", true, createdAt)
    ).take(4)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        statusList.forEachIndexed { index, (status, isActive, date) ->
            StatusTimelineItem(
                status = if (order.status == "Canceled" && index == 0) "Canceled" else status,
                isActive = if (order.status == "Canceled") false else isActive,
                date = if (order.status == "Canceled" && index == 0) canceledAt else date,
                isLast = index == statusList.size - 1
            )
        }
    }
}

@Composable
fun StatusTimelineItem(
    status: String,
    isActive: Boolean,
    date: String,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
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

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            if (isActive) Color(0xFF4FC3F7)
                            else Color(0xFFEEEEEE)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = date,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun OrderItemsSection(order: Order) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
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

        if (order.items.isEmpty()) {
            Text(
                text = "Không có sản phẩm trong đơn hàng",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            order.items.take(50).forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Log.d("OrderItemsSection", "Loading image: ${item.imageUrl}")
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = item.imageUrl,
                                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                                error = painterResource(id = android.R.drawable.ic_menu_report_image)
                            ),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f) // Đảm bảo Column không mở rộng quá mức
                        ) {
                            Text(
                                text = item.name ?: "Unknown",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
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
    val (address, phoneNumber) = remember(order.shippingAddress) {
        if (order.shippingAddress.contains("| Số điện thoại: ")) {
            val parts = order.shippingAddress.split("| Số điện thoại: ")
            parts[0].trim() to parts[1].trim()
        } else {
            order.shippingAddress to "Không có thông tin"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = "Thông tin giao hàng",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                Text(
                    text = address,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Số điện thoại: $phoneNumber",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}