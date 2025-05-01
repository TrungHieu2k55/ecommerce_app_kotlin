package com.example.duan.View.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.Product
import com.example.duan.View.components.BottomNavigationBar
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import com.example.duan.ViewModel.usecase.product.ProductViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Log

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    userName: String?,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val topSellingProducts by productViewModel.topSellingProducts
    val newInProducts by productViewModel.newInProducts
    val hoodiesProducts by productViewModel.hoodiesProducts
    val trendingProducts by productViewModel.trendingProducts
    val isLoading by productViewModel.isLoading
    val errorMessage by productViewModel.errorMessage

    LaunchedEffect(Unit) {
        productViewModel.fetchTopSellingProducts()
        productViewModel.fetchNewInProducts()
        productViewModel.fetchHoodiesProducts()
        productViewModel.fetchTrendingProducts()
    }

    Scaffold(
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
                TopBar(navController, userName, authViewModel)
            }

            item {
                SearchBar(navController)
            }

            item {
                CategorySection()
            }

            item {
                SectionHeader("Top Selling")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (errorMessage != null) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red
                        )
                    } else if (topSellingProducts.isEmpty()) {
                        Text(
                            text = "No Top Selling products available",
                            color = Color.Gray
                        )
                    } else {
                        TopSellingProductRow(
                            products = topSellingProducts,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                SectionHeader("New In")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (errorMessage != null) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red
                        )
                    } else if (newInProducts.isEmpty()) {
                        Text(
                            text = "No New In products available",
                            color = Color.Gray
                        )
                    } else {
                        NewInProductRow(
                            products = newInProducts,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                SectionHeader("Trending")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (errorMessage != null) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red
                        )
                    } else if (trendingProducts.isEmpty()) {
                        Text(
                            text = "No Trending products available",
                            color = Color.Gray
                        )
                    } else {
                        TrendingProductRow(
                            products = trendingProducts,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Hoodies (${hoodiesProducts.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (errorMessage != null) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red
                        )
                    } else if (hoodiesProducts.isEmpty()) {
                        Text(
                            text = "No Hoodies available",
                            color = Color.Gray
                        )
                    } else {
                        HoodiesGrid(
                            products = hoodiesProducts,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
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
fun TopBar(navController: NavController, user: String?, authViewModel: AuthViewModel) {
    // Lấy userProfile từ AuthViewModel
    val userProfile by authViewModel.userProfile.collectAsState()
    var imageLoadError by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                // Hiển thị ảnh hồ sơ người dùng
                if (userProfile?.photoUrl?.isNotEmpty() == true) {
                    val securePhotoUrl = userProfile?.photoUrl?.replace("http://", "https://") ?: ""
                    val painter = rememberAsyncImagePainter(
                        model = securePhotoUrl,
                        onState = { state ->
                            when (state) {
                                is AsyncImagePainter.State.Loading -> {
                                    Log.d("HomeScreen", "Loading profile image from URL: $securePhotoUrl")
                                }
                                is AsyncImagePainter.State.Success -> {
                                    Log.d("HomeScreen", "Profile image loaded successfully: $securePhotoUrl")
                                    imageLoadError = null
                                }
                                is AsyncImagePainter.State.Error -> {
                                    Log.e("HomeScreen", "Failed to load profile image: ${state.result.throwable.message}")
                                    imageLoadError = "Failed to load image: ${state.result.throwable.message}"
                                }
                                else -> {}
                            }
                        }
                    )

                    Image(
                        painter = painter,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hello, ${user?.substringBefore("@") ?: "Guest"}",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Categories",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF4FC3F7))
                .clickable { navController.navigate("my_cart") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Shopping bag",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Hiển thị lỗi nếu có
    if (imageLoadError != null) {
        Text(
            text = imageLoadError ?: "",
            color = Color.Red,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SearchBar(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp)
            .clickable { navController.navigate("search") }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search",
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CategorySection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "See All",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) { index ->
                val categoryName = when (index) {
                    0 -> "Hoodies"
                    1 -> "Shorts"
                    2 -> "Shoes"
                    3 -> "Bag"
                    else -> "Accessories"
                }
                CategoryItem(categoryName)
            }
        }
    }
}

@Composable
fun CategoryItem(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            // Category icon would go here
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "See All",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun TopSellingProductRow(
    products: List<Product>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                onClick = {
                    val productJson = Gson().toJson(product)
                    val encodedProductJson = URLEncoder.encode(productJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("product_details/$encodedProductJson")
                }
            )
        }
    }
}

@Composable
fun NewInProductRow(
    products: List<Product>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                onClick = {
                    val productJson = Gson().toJson(product)
                    val encodedProductJson = URLEncoder.encode(productJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("product_details/$encodedProductJson")
                }
            )
        }
    }
}

@Composable
fun TrendingProductRow(
    products: List<Product>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                onClick = {
                    val productJson = Gson().toJson(product)
                    val encodedProductJson = URLEncoder.encode(productJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("product_details/$encodedProductJson")
                }
            )
        }
    }
}

@Composable
fun HoodiesGrid(
    products: List<Product>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        products.chunked(2).forEach { productPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                productPair.forEach { product ->
                    Box(modifier = Modifier.weight(1f)) {
                        ProductItem(
                            product = product,
                            onClick = {
                                val productJson = Gson().toJson(product)
                                val encodedProductJson = URLEncoder.encode(productJson, StandardCharsets.UTF_8.toString())
                                navController.navigate("product_details/$encodedProductJson")
                            }
                        )
                    }
                }
                if (productPair.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(180.dp)
            .clickable(
                onClick = {
                    if (product.id.isNotEmpty()) {
                        onClick()
                    }
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            if (product.images.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(product.images.first()),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }

            IconButton(
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${product.price}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}