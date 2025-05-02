package com.example.duan.View.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.duan.Model.model.Order
import com.example.duan.Model.model.Product
import com.example.duan.View.Cart.MyCartScreen
import com.example.duan.View.home.CategoryProductsScreen
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Log
import com.example.duan.View.home.OrderDetailScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
    val scope = rememberCoroutineScope()

    val userId = authViewModel.getCurrentUserId()
    val userProfile by authViewModel.userProfile.collectAsState()

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
            composable(
                route = "category_products/{categoryName}",
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Unknown"
                Log.d("AppNavigation", "Navigated to category_products with categoryName: $categoryName")
                CategoryProductsScreen(
                    categoryName = categoryName,
                    navController = navController
                )
            }
            composable("search") {
                SearchScreen(navController = navController, authViewModel = authViewModel)
            }
            composable("notification") { NotificationsScreen(navController) }
            composable("order") { OrderScreen(navController) }
            composable("setting") { SettingScreen(navController, authViewModel) }
            composable("my_cart") {
                val cartViewModel: CartViewModel = hiltViewModel()
                val userIdForCart = userProfile?.uid
                LaunchedEffect(userIdForCart) {
                    userIdForCart?.let { cartViewModel.init(it) }
                }
                if (userIdForCart != null) {
                    MyCartScreen(
                        cartViewModel = cartViewModel,
                        navController = navController,
                        userId = userIdForCart
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = false }
                        }
                    }
                }
            }
            composable("product_details/{productJson}") { backStackEntry ->
                val productJson = backStackEntry.arguments?.getString("productJson")
                val decodedProductJson = productJson?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                val product = decodedProductJson?.let {
                    Gson().fromJson(it, Product::class.java)
                }
                val userIdForProduct = userProfile?.uid
                if (product != null && userIdForProduct != null) {
                    ProductDetailsScreen(
                        navController = navController,
                        cartViewModel = hiltViewModel(),
                        product = product,
                        userId = userIdForProduct
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            }
            composable("order_details/{orderJson}") { backStackEntry ->
                val orderJson = backStackEntry.arguments?.getString("orderJson")
                val decodedOrderJson = orderJson?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                val order = decodedOrderJson?.let {
                    Gson().fromJson(it, Order::class.java)
                }
//                if (order != null) {
//                    OrderDetailScreen(
//                        navController = navController,
//                        order = order
//                    )
//                } else {
//                    LaunchedEffect(Unit) {
//                        navController.navigate("order") {
//                            popUpTo("main") { inclusive = false }
//                        }
//                    }
//                }
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