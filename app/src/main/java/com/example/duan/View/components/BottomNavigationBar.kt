package com.example.duan.View.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.duan.ViewModel.usecase.auth.AuthViewModel

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        "home" to Icons.Default.Home,
        "favorite" to Icons.Default.Favorite,
        "order" to Icons.Default.ShoppingBag,
        "setting" to Icons.Default.Person
    )

    // Lấy route hiện tại để xác định mục nào đang được chọn
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lấy userId từ AuthViewModel
    val authViewModel: AuthViewModel = hiltViewModel()
    val userId = authViewModel.getCurrentUserId()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { (route, icon) ->
            BottomNavItem(
                icon = icon,
                isSelected = currentRoute == route || (route == "favorite" && currentRoute?.startsWith("favorite/") == true),
                onClick = {
                    // Điều hướng đến route tương ứng
                    when (route) {
                        "favorite" -> {
                            userId?.let { uid ->
                                navController.navigate("favorite/$uid") {
                                    // Tránh tạo stack trùng lặp
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } ?: run {
                                // Nếu userId null, điều hướng đến login
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = false }
                                }
                            }
                        }
                        else -> {
                            navController.navigate(route) {
                                // Tránh tạo stack trùng lặp
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) Color.Black else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )

        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .background(Color.Black)
            )
        }
    }
}