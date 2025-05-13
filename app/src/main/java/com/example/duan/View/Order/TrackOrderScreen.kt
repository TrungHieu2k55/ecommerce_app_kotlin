package com.example.duan.View.Order

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.duan.ViewModel.OrderViewModel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.duan.Model.repository.FirestoreRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    navController: NavController,
    orderId: String,
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val orders by orderViewModel.orders.collectAsState()
    val order = orders.find { it.orderId == orderId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val firestoreRepository = FirestoreRepository()

    if (order == null) {
        Text("Order not found", modifier = Modifier.padding(16.dp))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Track Order",
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(order.items.firstOrNull()?.imageUrl ?: ""),
                    contentDescription = order.items.firstOrNull()?.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        order.items.firstOrNull()?.name ?: "Unknown Item",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.0f", (order.items.firstOrNull()?.price?.toDouble() ?: 0.0))}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = Color.LightGray, thickness = 1.dp)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Order Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Expected Delivery Date",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = order.tracking?.estimatedDelivery ?: "N/A",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tracking ID",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = order.tracking?.trackingNumber ?: "N/A",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text("Order Status", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC") // Xử lý múi giờ UTC
            }
            val createdAtFormatted = order.createdAt?.let {
                try {
                    dateFormatter.parse(it)?.let { date -> dateFormatter.format(date) } ?: "N/A"
                } catch (e: Exception) {
                    Log.e("TrackOrderScreen", "Lỗi parse createdAt: ${e.message}")
                    "N/A"
                }
            } ?: "N/A"
            val inProgressAtFormatted = order.inProgressAt?.let {
                try {
                    dateFormatter.parse(it)?.let { date -> dateFormatter.format(date) } ?: "N/A"
                } catch (e: Exception) {
                    Log.e("TrackOrderScreen", "Lỗi parse inProgressAt: ${e.message}")
                    "N/A"
                }
            } ?: "N/A"
            val shippedAtFormatted = order.shippedAt?.let {
                try {
                    dateFormatter.parse(it)?.let { date -> dateFormatter.format(date) } ?: "Expected ${order.tracking?.estimatedDelivery ?: "N/A"}"
                } catch (e: Exception) {
                    Log.e("TrackOrderScreen", "Lỗi parse shippedAt: ${e.message}")
                    "Expected ${order.tracking?.estimatedDelivery ?: "N/A"}"
                }
            } ?: "Expected ${order.tracking?.estimatedDelivery ?: "N/A"}"
            val deliveredAtFormatted = order.deliveredAt?.let {
                try {
                    dateFormatter.parse(it)?.let { date -> dateFormatter.format(date) } ?: "N/A"
                } catch (e: Exception) {
                    Log.e("TrackOrderScreen", "Lỗi parse deliveredAt: ${e.message}")
                    "N/A"
                }
            } ?: "N/A"

            Column {
                OrderStatusItemWithLine(
                    status = "Order Placed",
                    date = createdAtFormatted,
                    isCompleted = true,
                    isFirst = true,
                    isLast = false
                )

                OrderStatusItemWithLine(
                    status = "In Progress",
                    date = inProgressAtFormatted,
                    isCompleted = order.status != "Pending",
                    isFirst = false,
                    isLast = false
                )

                OrderStatusItemWithLine(
                    status = "Shipped",
                    date = shippedAtFormatted,
                    isCompleted = order.status == "Shipped" || order.status == "Delivered",
                    isFirst = false,
                    isLast = false
                )

                OrderStatusItemWithLine(
                    status = "Delivered",
                    date = deliveredAtFormatted,
                    isCompleted = order.status == "Delivered",
                    isFirst = false,
                    isLast = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (order.status == "Processing" || order.status == "Shipped") {
                Button(
                    onClick = {
                        orderViewModel.cancelOrder(order.orderId)
                        scope.launch {
                            snackbarHostState.showSnackbar("Đơn hàng đã bị hủy!")
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "Cancel Order",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusItemWithLine(
    status: String,
    date: String,
    isCompleted: Boolean,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isCompleted) Color(0xFF4FC3F7) else Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
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
                        .height(60.dp)
                        .background(
                            color = if (isCompleted) Color(0xFF4FC3F7) else Color.LightGray
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = status,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) Color.Black else Color.Gray
            )
            Text(
                text = date,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = status,
                tint = Color(0xFF4FC3F7),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}