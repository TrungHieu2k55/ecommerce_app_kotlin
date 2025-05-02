package com.example.duan.View.search

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.api.ProductApiClient
import com.example.duan.Model.model.Product
import com.example.duan.Model.model.FilterState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.request.ImageRequest
import com.example.duan.R
import com.example.duan.View.components.FilterDialog
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userId = authViewModel.getCurrentUserId() ?: "guest"
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var recentSearches by rememberSaveable { mutableStateOf(loadRecentSearches(context)) }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var appliedFilters by remember { mutableStateOf<FilterState?>(null) }
    var filterError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery, appliedFilters) {
        try {
            val filteredResults = ProductApiClient.filterProducts(
                brand = appliedFilters?.brand,
                gender = appliedFilters?.gender,
                sortBy = appliedFilters?.sortBy,
                priceRange = appliedFilters?.priceRange,
                minRating = appliedFilters?.minRating
            )
            searchResults = if (searchQuery.isNotBlank()) {
                filteredResults.filter { it.name.contains(searchQuery, ignoreCase = true) }
            } else {
                filteredResults
            }
            println("Search results: ${searchResults.size} products")
            filterError = null
        } catch (e: Exception) {
            filterError = "Lỗi khi lọc sản phẩm: ${e.message}"
            searchResults = emptyList()
            println("Filter error: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                        },
                        placeholder = { Text("Tìm kiếm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotBlank() && !recentSearches.contains(searchQuery)) {
                                recentSearches = (listOf(searchQuery) + recentSearches).take(10)
                                saveRecentSearches(context, recentSearches)
                            }
                        }),
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Xóa")
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF4FC3F7),
                            unfocusedBorderColor = Color(0xFF4FC3F7)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (searchQuery.isBlank() && appliedFilters == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gần đây",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (recentSearches.isNotEmpty()) {
                        Text(
                            text = "Xóa tất cả",
                            fontSize = 14.sp,
                            color = Color(0xFF4FC3F7),
                            modifier = Modifier
                                .clickable {
                                    recentSearches = emptyList()
                                    saveRecentSearches(context, recentSearches)
                                }
                        )
                    }
                }
                LazyColumn {
                    items(recentSearches) { search ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = search,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .clickable { searchQuery = search }
                            )
                            IconButton(onClick = {
                                recentSearches = recentSearches - search
                                saveRecentSearches(context, recentSearches)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa",
                                    tint = Color(0xFF4FC3F7)
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Kết quả cho \"$searchQuery\"" else "Kết quả lọc",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${searchResults.size} sản phẩm",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                if (filterError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filterError!!,
                            fontSize = 16.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không có sản phẩm phù hợp",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { product ->
                            ProductItem(
                                product = product,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            onApply = { filterState ->
                appliedFilters = filterState
                showFilterDialog = false
            },
            onDismiss = {
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun ProductItem(product: Product, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val productJson = Gson().toJson(product)
                val encodedProductJson = URLEncoder.encode(productJson, StandardCharsets.UTF_8.toString())
                navController.navigate("product_details/$encodedProductJson")
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Product Image
                val imageUrl = product.images.firstOrNull()?.takeIf { it.isNotBlank() && it.startsWith("http") }

                if (imageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .build()
                        ),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_background),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Favorite Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .clickable { /* Add to favorites */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.Gray,
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
                        text = product.rating.toString(),
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
}

fun saveRecentSearches(context: Context, searches: List<String>) {
    val sharedPreferences = context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("recentSearches", searches.joinToString(","))
    editor.apply()
}

fun loadRecentSearches(context: Context): List<String> {
    val sharedPreferences = context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
    val searches = sharedPreferences.getString("recentSearches", "") ?: ""
    return if (searches.isBlank()) emptyList() else searches.split(",")
}