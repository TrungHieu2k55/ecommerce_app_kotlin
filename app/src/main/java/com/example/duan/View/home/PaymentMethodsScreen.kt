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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val paymentMethods = authViewModel.userProfile.value?.paymentMethods ?: emptyList()
    val context = LocalContext.current

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
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(paymentMethods) { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                text = "Credit & Debit Card",
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
                        .padding(16.dp),
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
                            text = "Add New Card",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    TextButton(
                        onClick = {
                            // Giả lập luồng liên kết BankCard
                            Toast.makeText(context, "BankCard integration not implemented", Toast.LENGTH_SHORT).show()
                            val updatedMethods = paymentMethods.toMutableList().apply { add("BankCard") }
                            authViewModel.updatePaymentMethods(updatedMethods)
                        }
                    ) {
                        Text(
                            text = "Link",
                            color = Color(0xFF1E88E5),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phần More Payment Options
            Text(
                text = "More Payment Options",
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
                Column {
                    PaymentOptionItem(
                        icon = Icons.Default.Lock, // Giả lập biểu tượng PayPal
                        name = "Paypal",
                        onLinkClick = {
                            Toast.makeText(context, "PayPal integration not implemented", Toast.LENGTH_SHORT).show()
                            val updatedMethods = paymentMethods.toMutableList().apply { add("Paypal") }
                            authViewModel.updatePaymentMethods(updatedMethods)
                        }
                    )
                    Divider()
                    PaymentOptionItem(
                        icon = Icons.Default.Phone, // Giả lập biểu tượng Apple Pay
                        name = "MoMo",
                        onLinkClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("momo://")
                                    setPackage("com.mservice.momotransfer")
                                }
                                context.startActivity(intent)
                                val updatedMethods = paymentMethods.toMutableList().apply { add("MoMo") }
                                authViewModel.updatePaymentMethods(updatedMethods)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Please install MoMo app", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Add Payment Method
            Button(
                onClick = {
                    // Giả lập hành động thêm phương thức mới
                    Toast.makeText(context, "Add new payment method", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBBDEFB), // Màu tím nhạt
                    contentColor = Color(0xFF1E88E5)
                )
            ) {
                Text(
                    text = "Add Payment Method",
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
            .padding(16.dp),
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
        TextButton(onClick = onLinkClick) {
            Text(
                text = "Link",
                color = Color(0xFF1E88E5),
                fontSize = 14.sp
            )
        }
    }
}