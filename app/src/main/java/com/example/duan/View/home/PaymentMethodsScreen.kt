package com.example.duan.View.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userId: String,
    totalCost: Double
) {
    val paymentMethods = authViewModel.userProfile.value?.paymentMethods ?: emptyList()
    val context = LocalContext.current
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") },
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
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Danh sách phương thức thanh toán đã liên kết (nếu có)
            if (paymentMethods.isNotEmpty()) {
                Text(
                    text = "Linked Payment Methods",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(paymentMethods) { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    selectedPaymentMethod = method
                                }
                                .background(
                                    if (selectedPaymentMethod == method) Color(0xFFE3F2FD) else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(method, fontSize = 16.sp)
                            IconButton(onClick = {
                                val updatedMethods = paymentMethods.toMutableList().apply { remove(method) }
                                authViewModel.updatePaymentMethods(updatedMethods)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                        Divider()
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phần Credit & Debit Card
            Text(
                text = "Payment Options",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            selectedPaymentMethod = "BankCard"
                        }
                        .background(
                            if (selectedPaymentMethod == "BankCard") Color(0xFFE3F2FD) else Color.White
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Credit Card",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Credit/Debit Card",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    if (selectedPaymentMethod == "BankCard") {
                        Text(
                            text = "Selected",
                            color = Color(0xFF1E88E5),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Thêm tùy chọn COD (Cash on Delivery)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            selectedPaymentMethod = "COD"
                        }
                        .background(
                            if (selectedPaymentMethod == "COD") Color(0xFFE3F2FD) else Color.White
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Money,
                            contentDescription = "Cash on Delivery",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Cash on Delivery (COD)",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    if (selectedPaymentMethod == "COD") {
                        Text(
                            text = "Selected",
                            color = Color(0xFF1E88E5),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Phần More Payment Options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    PaymentOptionItem(
                        icon = Icons.Default.Lock,
                        name = "Paypal",
                        onLinkClick = {
                            selectedPaymentMethod = "Paypal"
                            Toast.makeText(context, "PayPal integration not implemented", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Divider()
                    PaymentOptionItem(
                        icon = Icons.Default.Phone,
                        name = "MoMo",
                        onLinkClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("momo://")
                                    setPackage("com.mservice.momotransfer")
                                }
                                context.startActivity(intent)
                                selectedPaymentMethod = "MoMo"
                            } catch (e: Exception) {
                                Toast.makeText(context, "Please install MoMo app", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Confirm Payment
            Button(
                onClick = {
                    if (selectedPaymentMethod != null) {
                        // Giả lập xác nhận thanh toán (thay bằng điều hướng đến màn hình xác nhận thực tế)
                        Toast.makeText(context, "Payment confirmed with $selectedPaymentMethod. Total: ${"%.2f".format(totalCost)} VNĐ", Toast.LENGTH_LONG).show()
                        // Điều hướng về OrderScreen hoặc màn hình xác nhận (chưa có, giả lập popBackStack)
                        navController.popBackStack("checkout/$userId", inclusive = false)
                    } else {
                        Toast.makeText(context, "Please select a payment method", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBBDEFB),
                    contentColor = Color(0xFF1E88E5)
                ),
                enabled = selectedPaymentMethod != null
            ) {
                Text(
                    text = "Confirm Payment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PaymentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    onLinkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onLinkClick() }
            .background(
                if (name == "Paypal" || name == "MoMo") Color.White else Color(0xFFE3F2FD)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = Color(0xFF666666),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
        Text(
            text = "Link",
            color = Color(0xFF1E88E5),
            fontSize = 14.sp
        )
    }
}