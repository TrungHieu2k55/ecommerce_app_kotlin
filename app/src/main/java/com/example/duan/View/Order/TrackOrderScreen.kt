package com.example.duan.View.Order

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    navController: NavController,
    orderId: String,
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val orders by orderViewModel.orders.collectAsState()
    val order = orders.find { it.orderId == orderId }

    if (order == null) {
        Text("Order not found", modifier = Modifier.padding(16.dp))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(order.items.firstOrNull()?.imageUrl ?: ""),
                contentDescription = order.items.firstOrNull()?.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(order.items.firstOrNull()?.name ?: "Unknown Item", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "Qty: ${order.items.firstOrNull()?.quantity ?: 0}pcs",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "$${String.format("%.2f", (order.items.firstOrNull()?.price?.toDouble() ?: 0.0) )}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Order Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Expected Delivery Date: ${order.tracking?.estimatedDelivery ?: "N/A"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Tracking ID: ${order.tracking?.trackingNumber ?: "N/A"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Order Status", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Column {
                val createdAtFormatted = order.createdAt?.toDate()?.let {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
                } ?: "N/A"
                OrderStatusItem("Order Placed", createdAtFormatted, true)
                OrderStatusItem("In Progress", createdAtFormatted, order.status != "Pending")
                OrderStatusItem("Shipped", "Expected ${order.tracking?.estimatedDelivery ?: "N/A"}", order.status == "Shipped")
                OrderStatusItem("Delivered", "N/A", order.status == "Delivered")
            }
        }
    }
}

@Composable
fun OrderStatusItem(status: String, date: String, isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
            contentDescription = status,
            tint = if (isCompleted) Color(0xFF4FC3F7) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(status, fontSize = 14.sp, color = if (isCompleted) Color.Black else Color.Gray)
            Text(date, fontSize = 12.sp, color = Color.Gray)
        }
    }
}