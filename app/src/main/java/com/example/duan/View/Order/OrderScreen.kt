package com.example.duan.View.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.Order
import com.example.duan.ViewModel.cart.CartViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.duan.Model.model.CartItem
import com.example.duan.View.components.BottomNavigationBar
import com.example.duan.ViewModel.OrderViewModel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val orders by orderViewModel.orders.collectAsState()
    // Nhận tham số tab từ navigation
    val tab = navController.currentBackStackEntry?.arguments?.getString("tab") ?: "Active"
    var selectedTab by remember { mutableStateOf(tab) }

    // Tải lại dữ liệu khi vào màn hình
    LaunchedEffect(Unit) {
        orderViewModel.fetchOrders() // Giả sử có phương thức này trong OrderViewModel
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Tab Filter
            TabRow(
                selectedTabIndex = when (selectedTab) {
                    "Active" -> 0
                    "Completed" -> 1
                    "Cancelled" -> 2
                    else -> 0
                },
                containerColor = Color.White,
                contentColor = Color(0xFF4FC3F7),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[0]),
                        color = Color(0xFF4FC3F7)
                    )
                }
            ) {
                listOf("Active", "Completed", "Cancelled").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == label,
                        onClick = { selectedTab = label },
                        text = { Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Danh sách đơn hàng
            LazyColumn {
                items(orders.filter {
                    when (selectedTab) {
                        "Active" -> it.status == "Processing" || it.status == "Shipped"
                        "Completed" -> it.status == "Delivered"
                        "Cancelled" -> it.status == "Canceled"
                        else -> false
                    }
                }) { order ->
                    OrderItem(order = order, navController = navController, cartViewModel = cartViewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(order: Order, navController: NavController, cartViewModel: CartViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(order.items.firstOrNull()?.imageUrl ?: ""),
                    contentDescription = order.items.firstOrNull()?.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = order.items.firstOrNull()?.name ?: "Unknown Item",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Qty: ${order.items.firstOrNull()?.quantity ?: 0}pcs",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", (order.items.firstOrNull()?.price?.toDouble() ?: 0.0) )}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Button(
                onClick = {
                    when (order.status) {
                        "Delivered" -> navController.navigate("leave_review/${order.orderId}")
                        "Processing", "Shipped" -> navController.navigate("track_order/${order.orderId}")
                        "Canceled" -> {
                            // Logic Re-Order: Thêm lại các sản phẩm vào giỏ hàng
                            order.items.forEach { orderItem ->
                                cartViewModel.addToCart(
                                    item = CartItem(
                                        id = "", // ID sẽ được tạo tự động trong FirestoreRepository
                                        productId = orderItem.productId,
                                        productName = orderItem.name,
                                        image = orderItem.imageUrl,
                                        price = orderItem.price.toDouble(),
                                        quantity = orderItem.quantity
                                    )
                                )
                            }
                            navController.navigate("cart")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(40.dp)
                    .width(120.dp)
            ) {
                Text(
                    text = when (order.status) {
                        "Delivered" -> "Leave Review"
                        "Processing", "Shipped" -> "Track Order"
                        else -> "Re-Order"
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}