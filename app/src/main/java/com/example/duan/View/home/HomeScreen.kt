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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.Product
import com.example.duan.R
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
    val userId = authViewModel.getCurrentUserId() ?: return // Chuyển hướng đến đăng nhập nếu null

    // Sử dụng .collectAsState() để lấy giá trị từ StateFlow
    val topSellingProducts by productViewModel.topSellingProducts.collectAsState()
    val newInProducts by productViewModel.newInProducts.collectAsState()
    val hoodiesProducts by productViewModel.hoodiesProducts.collectAsState()
    val trendingProducts by productViewModel.trendingProducts.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()

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
                CategorySection(navController)
            }

            item {
                SectionHeader(
                    title = "Top Selling",
                    onSeeAllClick = {
                        navController.navigate("category_products/Top Selling")
                    }
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
                    } else if (topSellingProducts.isEmpty()) {
                        Text(
                            text = "No Top Selling products available",
                            color = Color.Gray
                        )
                    } else {
                        TopSellingProductRow(
                            products = topSellingProducts,
                            userId = userId,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "New In",
                    onSeeAllClick = {
                        navController.navigate("category_products/New In")
                    }
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
                    } else if (newInProducts.isEmpty()) {
                        Text(
                            text = "No New In products available",
                            color = Color.Gray
                        )
                    } else {
                        NewInProductRow(
                            products = newInProducts,
                            userId = userId,
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Trending",
                    onSeeAllClick = {
                        navController.navigate("category_products/Trending")
                    }
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
                    } else if (trendingProducts.isEmpty()) {
                        Text(
                            text = "No Trending products available",
                            color = Color.Gray
                        )
                    } else {
                        TrendingProductRow(
                            products = trendingProducts,
                            userId = userId,
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
                            userId = userId,
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
                        contentScale = ContentScale.Crop
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
fun CategorySection(navController: NavController) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            val categories = listOf(
                "Hoodies" to R.drawable.hoodiess,
                "Shorts" to R.drawable.shorts,
                "Shoes" to R.drawable.shoes,
                "Bag" to R.drawable.bag,
                "Accessories" to R.drawable.accessories,
                "Technology" to R.drawable.technology
            )

            items(categories) { (categoryName, iconResId) ->
                CategoryItem(
                    categoryName = categoryName,
                    iconResId = iconResId,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun CategoryItem(categoryName: String, iconResId: Int, navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .padding(vertical = 4.dp)
            .clickable {
                Log.d("CategoryItem", "Clicked on $categoryName, navigating to category_products/$categoryName")
                navController.navigate("category_products/$categoryName")
            }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$categoryName icon",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = categoryName,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
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
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
fun TopSellingProductRow(
    products: List<Product>,
    userId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                userId = userId,
                productViewModel = productViewModel,
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
    userId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                userId = userId,
                productViewModel = productViewModel,
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
    userId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                userId = userId,
                productViewModel = productViewModel,
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
    userId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = hiltViewModel()
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
                            userId = userId,
                            productViewModel = productViewModel,
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
fun ProductItem(
    product: Product,
    userId: String,
    onClick: () -> Unit,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(product.id, userId) {
        isFavorite = productViewModel.isProductFavorited(userId, product.id)
    }

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
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Product Image
            if (product.images.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(product.images.first()),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }

            // Favorite Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .clickable {
                        productViewModel.toggleFavorite(userId, product)
                        isFavorite = !isFavorite
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product Info Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Name
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Rating Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFD700), // Gold color for star
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = product.rating.toFloat().toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }

        // Price with bold styling
        Text(
            text = "$${product.price}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1E1E1E)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    categoryName: String,
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    // Chọn danh sách sản phẩm dựa trên categoryName sử dụng .collectAsState()
    val products by when (categoryName) {
        "Top Selling" -> productViewModel.topSellingProducts
        "New In" -> productViewModel.newInProducts
        "Trending" -> productViewModel.trendingProducts
        else -> productViewModel.categoryProducts
    }.collectAsState()

    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()

    val userId = hiltViewModel<AuthViewModel>().getCurrentUserId() ?: return

    // Gọi hàm fetch tương ứng dựa trên categoryName
    LaunchedEffect(categoryName) {
        when (categoryName) {
            "Top Selling" -> {
                if (productViewModel.topSellingProducts.value.isEmpty()) {
                    productViewModel.fetchTopSellingProducts()
                }
            }
            "New In" -> {
                if (productViewModel.newInProducts.value.isEmpty()) {
                    productViewModel.fetchNewInProducts()
                }
            }
            "Trending" -> {
                if (productViewModel.trendingProducts.value.isEmpty()) {
                    productViewModel.fetchTrendingProducts()
                }
            }
            else -> {
                productViewModel.fetchProductsByCategory(categoryName)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
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
                Text(
                    text = "$categoryName (${products.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                            text = "Error: $errorMessage",
                            color = Color.Red
                        )
                    } else if (products.isEmpty()) {
                        Text(
                            text = "No $categoryName products available",
                            color = Color.Gray
                        )
                    } else {
                        HoodiesGrid(
                            products = products,
                            userId = userId,
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

