package com.example.duan.View.Order

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.duan.ViewModel.OrderViewModel.OrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveReviewScreen(
    navController: NavController,
    orderId: String,
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    val error by orderViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đánh giá đơn hàng #$orderId",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Đánh giá của bạn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star.toFloat() }) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star $star",
                            tint = if (star <= rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nhận xét",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            BasicTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                decorationBox = { innerTextField ->
                    if (comment.isEmpty()) {
                        Text(
                            text = "Nhập nhận xét của bạn...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (error != null) {
                Text(
                    text = error ?: "Đã xảy ra lỗi",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (rating > 0) {
                        orderViewModel.submitReview(orderId, rating, comment)
                        scope.launch {
                            if (error == null) {
                                snackbarHostState.showSnackbar("Đánh giá đã được gửi!")
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar("Lỗi: $error")
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Vui lòng chọn số sao!")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7)
                )
            ) {
                Text(
                    text = "Gửi Đánh Giá",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}