package com.example.duan.View.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderDetailScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Order #456765",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Order status timeline
            OrderStatusTimeline()

            Spacer(modifier = Modifier.height(24.dp))

            // Order items
            OrderItemsSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Shipping details
            ShippingDetailsSection()
        }
    }
}

@Composable
fun OrderStatusTimeline() {
    Column(modifier = Modifier.fillMaxWidth()) {
        StatusTimelineItem("Delivered", false, "28 May")
        StatusTimelineItem("Shipped", true, "28 May")
        StatusTimelineItem("Order Confirmed", true, "28 May")
        StatusTimelineItem("Order Placed", true, "28 May")
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
            if (status == "Delivered") {
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
fun OrderItemsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order Items",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "View All",
                color = Color(0xFF4FC3F7),
                fontSize = 12.sp
            )
        }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = "Items",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "4 Items",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ShippingDetailsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Shipping details",
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
                    text = "2715 Ash Dr, San Jose, South Dakota 83475",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "123-234-7890",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewOrderDetailScreen() {
    MaterialTheme {
        OrderDetailScreen()
    }
}