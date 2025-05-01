package com.example.duan.View.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.duan.Model.model.Product
import com.example.duan.View.Cart.MyCartScreen
import com.example.duan.View.home.EditAddressScreen
import com.example.duan.View.home.EditProfilePictureScreen
import com.example.duan.View.home.HomeScreen
import com.example.duan.View.home.NotificationsScreen
import com.example.duan.View.home.OrderHistoryScreen
import com.example.duan.View.home.OrderScreen
import com.example.duan.View.home.PaymentMethodsScreen
import com.example.duan.View.home.SettingScreen
import com.example.duan.View.home.WelcomeScreen
import com.example.duan.View.product.ProductDetailsScreen
import com.example.duan.View.search.SearchScreen
import com.example.duan.ViewModel.cart.CartViewModel
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import com.example.duan.ViewModel.usecase.auth.LoginScreen
import com.example.duan.ViewModel.usecase.auth.RegisterScreen
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
    val scope = rememberCoroutineScope()

    val userId = authViewModel.getCurrentUserId()
    val userProfile by authViewModel.userProfile.collectAsState()

    // Tải dữ liệu giỏ hàng ngay khi có userId
    LaunchedEffect(userId) {
        if (userId != null) {
            cartViewModel.loadCartItems(userId)
        }
    }

    val startDestination = when {
        isFirstLaunch -> "wel"
        authViewModel.isLoggedIn.value -> "main"
        else -> "auth"
    }

    NavHost(navController, startDestination = startDestination) {
        navigation(startDestination = "welcome", route = "wel") {
            composable("welcome") {
                WelcomeScreen(
                    onStartShopping = {
                        scope.launch {
                            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
                            navController.navigate("register") {
                                popUpTo("wel") { inclusive = true }
                            }
                        }
                    },
                    onSignInClick = {
                        scope.launch {
                            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
                            navController.navigate("login") {
                                popUpTo("wel") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
        navigation(startDestination = "login", route = "auth") {
            composable("login") { LoginScreen(authViewModel, navController) }
            composable("register") { RegisterScreen(authViewModel, navController) }
        }
        navigation(startDestination = "home", route = "main") {
            composable("home") {
                HomeScreen(
                    authViewModel = authViewModel,
                    navController = navController,
                    userName = userProfile?.displayName
                )
            }
            composable("search") {
                SearchScreen(navController = navController, authViewModel = authViewModel)
            }
            composable("notification") { NotificationsScreen(navController) }
            composable("order") { OrderScreen(navController) }
            composable("setting") { SettingScreen(navController, authViewModel) }
            composable("my_cart") {
                MyCartScreen(cartViewModel, navController, userProfile?.uid ?: "")
            }
            composable("product_details/{productJson}") { backStackEntry ->
                val productJson = backStackEntry.arguments?.getString("productJson")
                val decodedProductJson = productJson?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                val product = decodedProductJson?.let {
                    Gson().fromJson(it, Product::class.java)
                }
                if (product != null) {
                    ProductDetailsScreen(
                        navController = navController,
                        cartViewModel = cartViewModel,
                        product = product,
                        userId = userProfile?.uid ?: ""
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            }
            composable("edit_profile") { EditProfilePictureScreen(navController, authViewModel) }
            composable("payment_methods") { PaymentMethodsScreen(navController, authViewModel) }
            composable("order_history") { OrderHistoryScreen(navController, authViewModel) }
            composable("edit_address") { EditAddressScreen(navController, authViewModel) }
        }
    }

    LaunchedEffect(isFirstLaunch) {
        if (!isFirstLaunch && navController.currentDestination?.route == "welcome") {
            navController.navigate(if (authViewModel.isLoggedIn.value) "main" else "auth") {
                popUpTo("wel") { inclusive = true }
            }
        }
    }
}