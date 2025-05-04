package com.example.duan.View.home

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
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
import com.example.duan.Model.api.createPayPalPayment
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userId: String,
    totalCost: Double,
    moMoResultLauncher: ActivityResultLauncher<Intent>
) {
    val paymentMethods = authViewModel.userProfile.value?.paymentMethods ?: emptyList()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var approvalUrl by remember { mutableStateOf<String?>(null) }

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
                        icon = Icons.Default.Phone,
                        name = "PayPal",
                        onLinkClick = {
                            if (totalCost <= 0) {
                                Log.d("PaymentMethodsScreen", "Total cost is <= 0, showing Toast")
                                Toast.makeText(context, "Số tiền phải lớn hơn 0", Toast.LENGTH_LONG).show()
                            } else {
                                coroutineScope.launch {
                                    try {
                                        val approvalUrlFromApi = createPayPalPayment(userId, totalCost)
                                        if (approvalUrlFromApi.isNullOrEmpty()) {
                                            Log.e("PaymentMethodsScreen", "Approval URL is null or empty")
                                            Toast.makeText(context, "Không thể tạo URL thanh toán PayPal", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                        Log.d("PayPal", "Approval URL: $approvalUrlFromApi")
                                        approvalUrl = approvalUrlFromApi
                                        selectedPaymentMethod = "PayPal"
                                        val customTabsIntent = CustomTabsIntent.Builder().build()
                                        customTabsIntent.launchUrl(context, Uri.parse(approvalUrlFromApi))
                                        approvalUrl = null // Reset sau khi mở
                                    } catch (e: Exception) {
                                        Log.e("PaymentMethodsScreen", "PayPal payment failed: ${e.toString()}", e)
                                        val errorMessage = e.message ?: "Lỗi không xác định"
                                        Toast.makeText(context, "Thanh toán PayPal thất bại: $errorMessage", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            approvalUrl?.let { url ->
                Log.d("PaymentMethodsScreen", "Displaying approval URL: $url")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = url,
                        fontSize = 14.sp,
                        color = Color.Blue,
                        modifier = Modifier.clickable {
                            val customTabsIntent = CustomTabsIntent.Builder().build()
                            customTabsIntent.launchUrl(context, Uri.parse(url))
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Click to open in browser",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedPaymentMethod != null) {
                        Toast.makeText(context, "Payment confirmed with $selectedPaymentMethod. Total: ${"%.2f".format(totalCost)} USD", Toast.LENGTH_LONG).show()
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
                if (name == "PayPal") Color.White else Color(0xFFE3F2FD)
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