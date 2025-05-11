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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.Order
import com.example.duan.View.components.BottomNavigationBar
import com.example.duan.ViewModel.OrderViewModel.OrderViewModel
import com.example.duan.ViewModel.cart.CartViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val orders by orderViewModel.orders.collectAsState()
    val tab = navController.currentBackStackEntry?.arguments?.getString("tab") ?: "Active"
    var selectedTab by remember { mutableStateOf(tab) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val reOrderResult by orderViewModel.reOrderResult.collectAsState()

    LaunchedEffect(reOrderResult) {
        reOrderResult?.let { result ->
            scope.launch {
                snackbarHostState.showSnackbar(result)
                if (result == "Đã thêm vào giỏ hàng!") {
                    navController.navigate("my_cart")
                }
                orderViewModel.resetReOrderResult()
            }
        }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                        modifier = Modifier.tabIndicatorOffset(tabPositions[when (selectedTab) {
                            "Active" -> 0
                            "Completed" -> 1
                            "Cancelled" -> 2
                            else -> 0
                        }]),
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

            // Loại bỏ trùng lặp ngay tại UI
            val filteredOrders = orders
                .filter {
                    when (selectedTab) {
                        "Active" -> it.status == "Processing" || it.status == "Shipped"
                        "Completed" -> it.status == "Delivered"
                        "Cancelled" -> it.status == "Canceled"
                        else -> false
                    }
                }
                .distinctBy { it.orderId } // Loại bỏ trùng lặp dựa trên orderId

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có đơn hàng trong trạng thái này",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    items(filteredOrders) { order ->
                        OrderItem(
                            order = order,
                            navController = navController,
                            cartViewModel = cartViewModel,
                            orderViewModel = orderViewModel
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(
    order: Order,
    navController: NavController,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel
) {
    var hasReviewed by remember { mutableStateOf(order.hasReviewed) }

    LaunchedEffect(order.orderId) {
        hasReviewed = order.hasReviewed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            // Hiển thị tất cả các item trong đơn hàng
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = item.imageUrl,
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                            error = painterResource(id = android.R.drawable.ic_menu_report_image)
                        ),
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Qty: ${item.quantity}pcs",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%.2f", item.price.toDouble())}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (order.items.indexOf(item) < order.items.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Nút hành động (Track, Leave Review, Re-Order)
            Button(
                onClick = {
                    when (order.status) {
                        "Delivered" -> {
                            if (!hasReviewed) {
                                navController.navigate("leave_review/${order.orderId}")
                            }
                        }
                        "Processing", "Shipped" -> navController.navigate("track_order/${order.orderId}")
                        "Canceled" -> {
                            orderViewModel.reOrderItems(order, cartViewModel)
                        }

                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .height(40.dp)
                    .width(120.dp)
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (order.status) {
                            "Delivered" -> if (hasReviewed) "Reviewed" else "Leave Review"
                            "Processing", "Shipped" -> "Track"
                            else -> "Re-Order"
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}