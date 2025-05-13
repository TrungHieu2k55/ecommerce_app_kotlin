package com.example.duan.View.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.Order
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    LaunchedEffect(Unit) {
        authViewModel.loadOrderHistory()
    }

    val orders by authViewModel.orderHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            EmptyOrdersView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(orders) { order ->
                    OrderItem(
                        order = order,
                        onClick = { navController.navigate("order_details/${order.orderId}") }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyOrdersView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "No Orders",
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No orders found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your order history will appear here once you make a purchase",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrderItem(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with order ID and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId.takeLast(8)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC") // Xử lý múi giờ UTC
                    }
                    val statusDate = when (order.status) {
                        "Delivered" -> order.deliveredAt?.let {
                            try {
                                dateFormatter.parse(it.toString())?.let { date -> dateFormatter.format(date) }
                                    ?: "N/A"
                            } catch (e: Exception) {
                                "N/A"
                            }
                        } ?: "N/A"
                        "Shipped" -> order.shippedAt?.let {
                            try {
                                dateFormatter.parse(it.toString())?.let { date -> dateFormatter.format(date) }
                                    ?: "N/A"
                            } catch (e: Exception) {
                                "N/A"
                            }
                        } ?: "N/A"
                        "Processing" -> order.inProgressAt?.let {
                            try {
                                dateFormatter.parse(it.toString())?.let { date -> dateFormatter.format(date) }
                                    ?: "N/A"
                            } catch (e: Exception) {
                                "N/A"
                            }
                        } ?: "N/A"
                        "Canceled" -> order.canceledAt?.let {
                            try {
                                dateFormatter.parse(it.toString())?.let { date -> dateFormatter.format(date) }
                                    ?: "N/A"
                            } catch (e: Exception) {
                                "N/A"
                            }
                        } ?: "N/A"
                        else -> order.createdAt?.let {
                            try {
                                dateFormatter.parse(it)?.let { date -> dateFormatter.format(date) }
                                    ?: "N/A"
                            } catch (e: Exception) {
                                "N/A" // Xử lý lỗi parse
                            }
                        } ?: "N/A"
                    }
                    Text(
                        text = statusDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (order.status == "Delivered" && order.hasReviewed) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEEEEEE)
                        ) {
                            Text(
                                text = "Reviewed",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    OrderStatusChip(status = order.status)
                }
            }

            Divider(
                color = Color(0xFFEEEEEE),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Order items
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                order.items.take(2).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Qty: ${item.quantity}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", item.price.toDouble())}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4FC3F7)
                            )
                        }
                    }
                }

                if (order.items.size > 2) {
                    Text(
                        text = "+${order.items.size - 2} more items",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Divider(
                color = Color(0xFFEEEEEE),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Total and view details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$${String.format("%.2f", order.totalPrice.toDouble())}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4FC3F7)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7).copy(alpha = 0.1f),
                    contentColor = Color(0xFF4FC3F7)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "View Order Details",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Delivered" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Shipped" -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
        "Pending" -> Color(0xFFFFF8E1) to Color(0xFFFFA000)
        "Processing" -> Color(0xFFFFF8E1) to Color(0xFFFFA000)
        "Canceled" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else -> Color(0xFFF5F5F5) to Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}