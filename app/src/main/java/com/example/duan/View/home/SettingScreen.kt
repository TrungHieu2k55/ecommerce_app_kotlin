package com.example.duan.View.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.duan.View.components.BottomNavigationBar
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import android.util.Log

@Composable
fun SettingScreen(navController: NavController, authViewModel: AuthViewModel) {
    var isSigningOut by remember { mutableStateOf(false) }
    var imageLoadError by remember { mutableStateOf<String?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    // Collect userProfile StateFlow
    val userProfile by authViewModel.userProfile.collectAsState()
    val userId = authViewModel.getCurrentUserId() // Lấy userId từ AuthViewModel

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF5F5F5), Color.White)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 40.dp)
            ) {
                // Profile section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .shadow(8.dp, CircleShape)
                            .background(Color.White)
                            .clickable { showFullScreenImage = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile?.photoUrl?.isNotEmpty() == true) {
                            val securePhotoUrl = userProfile?.photoUrl?.replace("http://", "https://") ?: ""
                            val painter = rememberAsyncImagePainter(
                                model = securePhotoUrl,
                                onState = { state ->
                                    when (state) {
                                        is AsyncImagePainter.State.Loading -> {
                                            Log.d("SettingScreen", "Loading image from URL: $securePhotoUrl")
                                        }
                                        is AsyncImagePainter.State.Success -> {
                                            Log.d("SettingScreen", "Image loaded successfully: $securePhotoUrl")
                                            imageLoadError = null
                                        }
                                        is AsyncImagePainter.State.Error -> {
                                            Log.e("SettingScreen", "Failed to load image: ${state.result.throwable.message}")
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
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (imageLoadError != null) {
                        Text(
                            text = imageLoadError ?: "",
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = userProfile?.displayName ?: "Guest",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = userProfile?.email ?: "No email",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Sắp xếp số điện thoại và nút Edit theo chiều dọc
                    Column(
                        modifier = Modifier
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = userProfile?.phoneNumber ?: "No phone",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { navController.navigate("edit_profile") },
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4FC3F7).copy(alpha = 0.1f)),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF4FC3F7)
                            )
                        ) {
                            Text(
                                text = "Edit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Settings options
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        SettingItem("Address") { navController.navigate("edit_address") }
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        SettingItem("Wishlist") { navController.navigate("wishlist") }
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        SettingItem("Order History") { navController.navigate("order_history") }
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        SettingItem("Help") { navController.navigate("help") }
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        SettingItem("Support") { navController.navigate("support") }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign out button
                Button(
                    onClick = {
                        isSigningOut = true
                        authViewModel.logout()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    enabled = !isSigningOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4FC3F7),
                        contentColor = Color.White
                    )
                ) {
                    if (isSigningOut) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Sign Out",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Hiển thị ảnh phóng to khi bấm vào ảnh hồ sơ
            if (showFullScreenImage && userProfile?.photoUrl?.isNotEmpty() == true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { showFullScreenImage = false },
                    contentAlignment = Alignment.Center
                ) {
                    val securePhotoUrl = userProfile?.photoUrl?.replace("http://", "https://") ?: ""
                    Image(
                        painter = rememberAsyncImagePainter(securePhotoUrl),
                        contentDescription = "Full Screen Profile Picture",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            if (isSigningOut) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SettingItem(title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go to $title",
            tint = Color(0xFF666666)
        )
    }
}