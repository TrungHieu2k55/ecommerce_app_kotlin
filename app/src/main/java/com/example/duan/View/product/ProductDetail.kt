package com.example.duan.View.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.model.Product
import com.example.duan.Model.model.Review
import com.example.duan.ViewModel.cart.CartViewModel
import com.example.duan.ViewModel.usecase.product.ProductViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    product: Product,
    userId: String,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    var selectedSize by remember { mutableStateOf(if (product.sizes.isNotEmpty()) product.sizes.first() else "N/A") }
    var selectedColor by remember { mutableStateOf(if (product.colors.isNotEmpty()) product.colors.first() else "#000000") }
    var isFavorite by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf(product.images.firstOrNull() ?: "") }
    var expandedDescription by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(1) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tăng lượt xem khi màn hình được tải lần đầu
    LaunchedEffect(product.id) {
        if (product.id.isNotEmpty()) {
            productViewModel.incrementProductViews(product.id)
        }
    }

    // Khởi tạo CartViewModel với userId
    LaunchedEffect(userId) {
        cartViewModel.init(userId)
    }

    // Lấy dữ liệu reviews từ ViewModel
    LaunchedEffect(product.id) {
        productViewModel.fetchReviews(product.id)
    }
    val reviews by productViewModel.reviews.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Lỗi khi tải đánh giá: $it")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi Tiết Sản Phẩm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF4FC3F7)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Yêu thích",
                            tint = if (isFavorite) Color.Red else Color(0xFF4FC3F7),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tổng Giá",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${(product.price * quantity).toDouble()} $",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4FC3F7)
                        )
                    }

                    Button(
                        onClick = {
                            // Kiểm tra size
                            if (product.sizes.isNotEmpty() && selectedSize == "N/A") {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Vui lòng chọn kích thước")
                                }
                                return@Button
                            }
                            // Kiểm tra color
                            if (product.colors.isNotEmpty() && selectedColor == "#000000") {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Vui lòng chọn màu sắc")
                                }
                                return@Button
                            }
                            // Kiểm tra quantity
                            if (quantity <= 0) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Vui lòng chọn số lượng hợp lệ")
                                }
                                return@Button
                            }
                            // Kiểm tra image
                            if (selectedImage.isEmpty()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Vui lòng chọn hình ảnh")
                                }
                                return@Button
                            }

                            // Thêm vào giỏ hàng nếu tất cả điều kiện thỏa mãn
                            val cartItem = CartItem(
                                id = product.id + System.currentTimeMillis().toString(),
                                productId = product.id,
                                productName = product.name,
                                price = product.price,
                                quantity = quantity,
                                image = selectedImage,
                                size = selectedSize,
                                color = selectedColor
                            )
                            cartViewModel.addToCart(cartItem)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Đã thêm vào giỏ hàng thành công!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4FC3F7)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .width(200.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Giỏ hàng",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thêm vào Giỏ",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            item {
                // Hình ảnh chính với gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    if (selectedImage.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImage),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không có hình ảnh", color = Color.Gray)
                        }
                    }

                    // Thêm gradient overlay ở dưới ảnh
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.3f)
                                    ),
                                    startY = 300f
                                )
                            )
                    )
                }

                // Danh sách ảnh phụ trong card có border
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-20).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(product.images) { imageUrl ->
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(
                                        width = if (imageUrl == selectedImage) 2.dp else 0.dp,
                                        color = if (imageUrl == selectedImage) Color(0xFF4FC3F7) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                                    .clickable { selectedImage = imageUrl }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = "Hình ảnh sản phẩm",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(if (imageUrl == selectedImage) 2.dp else 0.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Thông tin sản phẩm
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Thể loại
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFECE3),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = "Phong Cách",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4FC3F7),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tên và đánh giá
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF8E1),
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(start = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Đánh giá",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = product.rating.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF795548)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Mô tả
                        Text(
                            text = "Chi Tiết Sản Phẩm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            maxLines = if (expandedDescription) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = if (expandedDescription) "Thu gọn" else "Xem thêm",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFA52A2A),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { expandedDescription = !expandedDescription }
                        )
                    }
                }

                // Kích thước
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Chọn Kích Thước",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (product.sizes.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(product.sizes) { size ->
                                    val isSelected = size == selectedSize
                                    Surface(
                                        onClick = { selectedSize = size },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFF4FC3F7) else Color.White,
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF4FC3F7) else Color.LightGray),
                                        modifier = Modifier
                                            .size(50.dp)
                                            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(12.dp))
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = size,
                                                color = if (isSelected) Color.White else Color.Black,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Không có kích thước",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Màu sắc
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Chọn Màu Sắc",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (product.colors.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(product.colors) { color ->
                                    val isSelected = color == selectedColor
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(color)))
                                            .clickable { selectedColor = color }
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) Color.Black else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .padding(2.dp)
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Không có màu sắc",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Số lượng
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4FC3F7)
                    ),
                    elevation = cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Chọn Số Lượng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Nút giảm số lượng
                            Surface(
                                onClick = {
                                    if (quantity > 1) quantity--
                                },
                                shape = CircleShape,
                                color = Color(0xFFF1F1F1),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "-",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }

                            // Hiển thị số lượng
                            Text(
                                text = quantity.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            // Nút tăng số lượng
                            Surface(
                                onClick = { quantity++ },
                                shape = CircleShape,
                                color = Color(0xFFF1F1F1),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }

                // Phần Reviews
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ĐÁNH GIÁ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Text(
                                    text = "${reviews.size} Đánh giá",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (reviews.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có đánh giá",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            reviews.forEach { review ->
                                ReviewItem(review)
                                if (review != reviews.last()) {
                                    Divider(
                                        color = Color(0xFFEEEEEE),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh đại diện người dùng
            Card(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                elevation = cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                ) {
                    if (review.userAvatar.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(review.userAvatar),
                            contentDescription = "Ảnh đại diện",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = review.userName.firstOrNull()?.toString() ?: "?",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin người dùng và thời gian
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = review.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Đánh giá sao
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Sao",
                                tint = if (i <= review.rating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    // Thời gian
                    Text(
                        text = review.timestamp?.toDate()?.let {
                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
                        } ?: "Ngày không xác định",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nội dung đánh giá
        Text(
            text = review.comment,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp)
        )
    }
}

fun Long.toStringDecimal(): String {
    return "%.2f".format(this.toDouble() / 100)
}