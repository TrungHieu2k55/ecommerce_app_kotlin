package com.example.duan.View.checkout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.ShippingOption
import com.example.duan.Model.model.CartItem
import com.example.duan.ViewModel.cart.CartViewModel
import com.example.duan.ViewModel.usecase.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    userId: String,
    cartViewModel: CartViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    val buttonColor = Color(0xFF29B6F6)
    val lightGrayColor = Color(0xFFF5F5F5)
    val changeButtonColor = Color(0xFF29B6F6)

    // Khởi tạo CartViewModel với userId
    LaunchedEffect(userId) {
        cartViewModel.init(userId)
    }

    val items by cartViewModel.cartItems.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val selectedAddress = remember(userProfile) {
        userProfile?.addresses?.find { it.title == userProfile?.selectedAddress }?.details ?: "Default Address"
    }

    var selectedShippingOption by remember {
        mutableStateOf(
            ShippingOption(
                "1",
                "Giao hàng tiết kiệm",
                "07 Tháng 05 2025",
                20000.0
            )
        )
    }

    val discount by cartViewModel.discount.collectAsState()

    // Tính toán tổng chi phí trực tiếp từ cartItems
    val subTotal = remember(items) {
        items.sumOf { (it.price ?: 0L).toDouble() / 100 * it.quantity }
    }
    val deliveryFee = selectedShippingOption.deliveryFee
    val totalCost = subTotal + deliveryFee - discount

    val formattedSubTotal = remember(subTotal) { "%.2f".format(subTotal) }
    val formattedDeliveryFee = remember(deliveryFee) { "%.2f".format(deliveryFee) }
    val formattedDiscount = remember(discount) { "%.2f".format(discount) }
    val formattedTotalCost = remember(totalCost) { "%.2f".format(totalCost) }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.get<Map<String, Any?>>("selectedShippingOption")?.let { optionMap ->
            selectedShippingOption = ShippingOption(
                id = optionMap["id"] as String,
                name = optionMap["name"] as String,
                estimatedArrival = optionMap["estimatedArrival"] as String,
                deliveryFee = optionMap["deliveryFee"] as Double,
                address = optionMap["address"] as String?
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Thanh toán",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = lightGrayColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "Địa chỉ giao hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nhà\n$selectedAddress",
                            fontSize = 14.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = {
                            navController.navigate("edit_address") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = changeButtonColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(32.dp)
                    ) {
                        Text(
                            text = "THAY ĐỔI",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = lightGrayColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "Chọn phương thức vận chuyển",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedShippingOption.name}\nDự kiến giao ${selectedShippingOption.estimatedArrival}",
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = {
                            navController.navigate("shipping_type")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = changeButtonColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(32.dp)
                    ) {
                        Text(
                            text = "THAY ĐỔI",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = "Danh sách đơn hàng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(items) { item ->
                    CheckoutItem(item = item)
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sub-Total", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$formattedSubTotal VNĐ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Delivery Fee", fontSize = 14.sp, color = Color.Gray)
                    Text(text = "$formattedDeliveryFee VNĐ", fontSize = 14.sp, color = Color.Gray)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Discount", fontSize = 14.sp, color = Color.Gray)
                    Text(text = "-$formattedDiscount VNĐ", fontSize = 14.sp, color = Color.Red)
                }
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Cost", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$formattedTotalCost VNĐ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Điều hướng đến PaymentMethodsScreen với userId và totalCost
                        navController.navigate("payment_methods/$userId?totalCost=$totalCost")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                ) {
                    Text(
                        text = "Tiếp tục thanh toán",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutItem(item: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(item.image),
            contentDescription = item.productName,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.productName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Kích thước: ${item.size ?: "N/A"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            val formattedPrice = remember(item.price) { (item.price ?: 0L).toDouble() / 100 }
            Text(
                text = "${String.format("%.2f", formattedPrice)} VNĐ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}