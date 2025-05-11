package com.example.duan.View.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.duan.View.components.BottomNavigationBar
import com.example.duan.ViewModel.usecase.product.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    userId: String,
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val favoritedProducts by productViewModel.favoritedProducts.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()

    LaunchedEffect(userId) {
        productViewModel.fetchFavoritedProducts(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh Sách Yêu Thích của Tôi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Nút lọc theo danh mục
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("Tất cả", "Áo khoác", "Áo sơ mi", "Quần", "Áo thun")
                    items(categories) { category ->
                        FilterButton(
                            text = category,
                            isSelected = category == "Áo khoác", // Cập nhật logic này dựa trên lựa chọn của người dùng
                            onClick = { /* Xử lý logic lọc */ }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (errorMessage != null) {
                        Text(
                            text = "Lỗi: $errorMessage",
                            color = Color.Red
                        )
                    } else if (favoritedProducts.isEmpty()) {
                        Text(
                            text = "Không có sản phẩm yêu thích nào",
                            color = Color.Gray
                        )
                    } else {
                        HoodiesGrid(
                            products = favoritedProducts,
                            userId = userId,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth(),
                            productViewModel = productViewModel
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4FC3F7) else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text)
    }
}