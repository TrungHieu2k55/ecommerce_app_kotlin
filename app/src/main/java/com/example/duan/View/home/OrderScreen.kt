package com.example.duan.View.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.duan.View.components.BottomNavigationBar

@Composable
fun OrderScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Orders",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 26.dp)
            )

            // Filter Chips in a scrollable row
            OrderFilterChips()

            Spacer(modifier = Modifier.height(16.dp))

            // Order items
            OrdersList()
        }
    }
}

@Composable
fun OrderFilterChips() {
    var selectedChip by remember { mutableStateOf(0) }
    val chipLabels = listOf("Processing", "Shipped", "Delivered", "Returned", "Canceled")

    ScrollableTabRow(
        selectedTabIndex = selectedChip,
        edgePadding = 0.dp,
        divider = {},
        indicator = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        chipLabels.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedChip == index,
                onClick = { selectedChip = index },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4FC3F7),
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun OrdersList() {
    Column(modifier = Modifier.fillMaxWidth()) {
        OrderItem("Order #456766", "4 items")
        Spacer(modifier = Modifier.height(8.dp))
        OrderItem("Order #454569", "2 items")
        Spacer(modifier = Modifier.height(8.dp))
        OrderItem("Order #454809", "1 items")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(orderNumber: String, itemCount: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF6F6F6)
        ),
        onClick = { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = "Order",
                    modifier = Modifier.size(24.dp),
                    tint = Color.DarkGray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = orderNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = itemCount,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = "View Order",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyOrdersView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = "No Orders",
            modifier = Modifier.size(80.dp),
            tint = Color.Yellow
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Orders yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B6BFF))
        ) {
            Text("Explore Categories")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewOrdersContent() {
    MaterialTheme {
        OrderScreen(navController = rememberNavController())
    }
}