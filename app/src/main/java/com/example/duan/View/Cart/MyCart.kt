package com.example.duan.View.Cart

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.duan.Model.model.CartItem
import com.example.duan.ViewModel.cart.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCartScreen(
    cartViewModel: CartViewModel,
    navController: NavController,
    userId: String? = "userId" // Thay bằng userId thực tế (lấy từ AuthViewModel)
) {
    // Màu sắc
    val buttonColor = Color(0xFF29B6F6) // Màu xanh dương nhạt
    val lightGrayColor = Color(0xFFF5F5F5)
    val redColor = Color(0xFFFF6B6B)

    // Trạng thái mã giảm giá
    var promoCode by remember { mutableStateOf("") }

    // Lấy danh sách sản phẩm từ ViewModel
    val items by cartViewModel.cartItems // Sửa: Loại bỏ observeAsState

    // Tính toán tổng chi phí
    val subTotal = items.sumOf { it.price * it.quantity }.toDouble() // Ép kiểu thành Double
    val deliveryFee = 0.0 // Sử dụng Double thay vì Int
    val discount = 0.0 // Sử dụng Double thay vì Int
    val totalCost = subTotal + deliveryFee - discount

    // Định dạng giá trị trước để sử dụng trong composable
    val formattedSubTotal = remember(subTotal) { "%.2f".format(subTotal) }
    val formattedDeliveryFee = remember(deliveryFee) { "%.2f".format(deliveryFee) }
    val formattedDiscount = remember(discount) { "%.2f".format(discount) }
    val formattedTotalCost = remember(totalCost) { "%.2f".format(totalCost) }

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
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "My Cart",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // Danh sách sản phẩm với Swipe to Delete
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(items, key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                // Xóa mục khi vuốt sang trái
                                userId?.let { cartViewModel.removeFromCart(it, item) }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false, // Chỉ cho phép vuốt từ phải sang trái
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) redColor else Color.Transparent
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        content = {
                            CartItem(
                                item = item,
                                buttonColor = buttonColor,
                                onQuantityChange = { newQuantity ->
                                    cartViewModel.updateQuantity(userId, item, newQuantity)
                                }
                            )
                        }
                    )
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }

            // Promo Code Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it },
                    placeholder = { Text("Promo Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(32.dp),
                    trailingIcon = {
                        Button(
                            onClick = { /* Apply promo code */ },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Apply",
                                color = Color.White
                            )
                        }
                    }
                )
            }

            // Order Summary
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                OrderSummaryRow(title = "Sub-Total", value = "$$formattedSubTotal")
                OrderSummaryRow(title = "Delivery Fee", value = "$$formattedDeliveryFee")
                OrderSummaryRow(title = "Discount", value = "-$$formattedDiscount", valueColor = redColor)
                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                OrderSummaryRow(title = "Total Cost", value = "$$formattedTotalCost", isBold = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkout Button
            Button(
                onClick = { navController.navigate("checkout") },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Proceed to Checkout",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CartItem(
    item: CartItem,
    buttonColor: Color,
    onQuantityChange: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(item.quantity) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image
        Image(
            painter = rememberAsyncImagePainter(item.image), // Sửa: Dùng item.image thay vì URL cứng
            contentDescription = item.productName,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Product Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.productName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Định dạng giá trị price trước
            val formattedPrice = remember(item.price) { "%.2f".format(item.price) }
            Text(
                text = "$$formattedPrice",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Quantity Controls
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrease Button
            IconButton(
                onClick = {
                    if (quantity > 1) {
                        quantity--
                        onQuantityChange(quantity)
                    }
                },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.LightGray, CircleShape)
            ) {
                Text(
                    text = "−",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            // Quantity
            Text(
                text = quantity.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Increase Button
            IconButton(
                onClick = {
                    quantity++
                    onQuantityChange(quantity)
                },
                modifier = Modifier
                    .size(32.dp)
                    .background(buttonColor, CircleShape)
            ) {
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OrderSummaryRow(
    title: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color.Gray
        )

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}