package com.example.duan.View.Cart

import android.util.Log
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.model.Coupon
import com.example.duan.Model.model.Order
import com.example.duan.Model.model.OrderItem
import com.example.duan.ViewModel.cart.CartViewModel
import com.example.duan.ViewModel.usecase.product.CouponViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Hàm hỗ trợ chuyển đổi String thành Date
fun parseDate(dateString: String?): Date? {
    if (dateString.isNullOrEmpty()) return null
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.parse(dateString)
    } catch (e: Exception) {
        Log.e("MyCart", "Failed to parse date: $dateString, error: ${e.message}")
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCartScreen(
    cartViewModel: CartViewModel,
    navController: NavController,
    userId: String,
    couponViewModel: CouponViewModel = hiltViewModel()
) {
    // Màu sắc
    val buttonColor = Color(0xFF29B6F6)
    val lightGrayColor = Color(0xFFF5F5F5)
    val redColor = Color(0xFFFF6B6B)

    // Trạng thái mã giảm giá
    var promoCode by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf(0.0) }
    val appliedCoupon by couponViewModel.appliedCoupon.collectAsState()
    val couponError by couponViewModel.errorMessage.collectAsState()

    // Trạng thái hiển thị danh sách mã giảm giá
    val sheetState = rememberModalBottomSheetState()
    var showCouponsSheet by remember { mutableStateOf(false) }

    // Trạng thái thông báo lỗi khi đặt hàng
    var orderError by remember { mutableStateOf<String?>(null) }

    // Khởi tạo ViewModel với userId
    LaunchedEffect(userId) {
        cartViewModel.init(userId)
    }

    // Lấy danh sách sản phẩm từ ViewModel
    val items by cartViewModel.cartItems.collectAsState()

    // Lấy danh sách mã giảm giá
    val coupons by couponViewModel.coupons.collectAsState()
    LaunchedEffect(Unit) {
        couponViewModel.fetchCoupons()
    }

    // Tính toán tổng chi phí trực tiếp từ cartItems (đơn vị USD)
    val subTotal = remember(items) {
        items.sumOf { (it.price ?: 0L).toDouble() * it.quantity }
    }
    val deliveryFee = 0.0
    val totalCost = subTotal + deliveryFee - discount

    // Định dạng giá trị
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

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(items, key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                cartViewModel.removeFromCart(item.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
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
                                    cartViewModel.updateQuantity(item.id, newQuantity)
                                }
                            )
                        }
                    )
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(onClick = { showCouponsSheet = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Xem mã giảm giá")
                            }
                            if (promoCode.isNotEmpty()) {
                                IconButton(onClick = { promoCode = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                            Button(
                                onClick = {
                                    if (promoCode.isNotBlank()) {
                                        couponViewModel.applyCoupon(promoCode, (subTotal * 100).toLong()) { discountAmount ->
                                            discount = discountAmount.toDouble()
                                            cartViewModel.setDiscount(discount) // Lưu discount vào ViewModel
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                shape = RoundedCornerShape(32.dp),
                                modifier = Modifier
                                    .height(40.dp)
                            ) {
                                Text(
                                    text = "Apply",
                                    color = Color.White
                                )
                            }
                        }
                    }
                )
            }

            couponError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )
            }
            appliedCoupon?.let {
                Text(
                    text = "Applied Coupon: ${it.code} (-${formattedDiscount} USD)",
                    color = Color.Green,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                OrderSummaryRow(title = "Sub-Total", value = "$formattedSubTotal USD")
                OrderSummaryRow(title = "Delivery Fee", value = "$formattedDeliveryFee USD")
                OrderSummaryRow(
                    title = "Discount",
                    value = "-$formattedDiscount USD",
                    valueColor = redColor
                )
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OrderSummaryRow(
                    title = "Total Cost",
                    value = "$formattedTotalCost USD",
                    isBold = true
                )
            }

            orderError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val orderItems = items.map { cartItem ->
                        OrderItem(
                            productId = cartItem.productId,
                            name = cartItem.productName,
                            imageUrl = cartItem.image,
                            quantity = cartItem.quantity,
                            price = cartItem.price ?: 0L
                        )
                    }
                    val order = Order(
                        orderId = System.currentTimeMillis().toString(),
                        userId = userId,
                        items = orderItems,
                        totalPrice = (totalCost * 100).toLong(), // Lưu totalPrice ở USD (đã nhân 100 để bỏ thập phân)
                        status = "Processing",
                        createdAt = Timestamp.now(),
                        shippingAddress = "Address",
                        couponCode = appliedCoupon?.code,
                        discount = (discount * 100).toLong()
                    )

                    Firebase.firestore.collection("users")
                        .document(userId)
                        .collection("orders")
                        .document(order.orderId)
                        .set(order)
                        .addOnSuccessListener {
                            orderError = null
                            navController.navigate("checkout/$userId")
                        }
                        .addOnFailureListener { e ->
                            orderError = "Failed to place order: ${e.message}"
                        }
                },
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

        if (showCouponsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCouponsSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Available Coupons",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (coupons.isEmpty()) {
                        Text(
                            text = "No coupons available",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(coupons) { coupon ->
                                CouponItem(
                                    coupon = coupon,
                                    onApply = {
                                        promoCode = coupon.code
                                        couponViewModel.applyCoupon(coupon.code, (subTotal * 100).toLong()) { discountAmount ->
                                            discount = discountAmount.toDouble()
                                            cartViewModel.setDiscount(discount)
                                        }
                                        showCouponsSheet = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showCouponsSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Close",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CouponItem(
    coupon: Coupon,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onApply() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = coupon.code,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (coupon.type == "percentage")
                        "Get ${coupon.discount}% off (up to ${coupon.maxDiscount.toDouble().toStringDecimal()} USD)"
                    else
                        "Get ${coupon.discount.toDouble().toStringDecimal()} USD off",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Min. order: ${coupon.minOrderValue.toDouble().toStringDecimal()} USD",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                val validToDate = parseDate(coupon.validTo)
                Text(
                    text = "Valid until: ${
                        validToDate?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: "N/A"
                    }",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F7)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .height(40.dp)
            ) {
                Text(
                    text = "Apply",
                    color = Color.White,
                    fontSize = 14.sp
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
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            val formattedPrice = remember(item.price) { (item.price ?: 0L).toDouble() }
            Text(
                text = "${String.format("%.2f", formattedPrice)} USD",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Text(
                text = quantity.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

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

fun Double.toStringDecimal(): String {
    return String.format("%.2f", this)
}