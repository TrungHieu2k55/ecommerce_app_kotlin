package com.example.duan.View.navigation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.duan.Model.model.Product
import com.example.duan.View.Cart.MyCartScreen
import com.example.duan.View.Order.LeaveReviewScreen
import com.example.duan.View.Order.TrackOrderScreen
import com.example.duan.View.checkout.CheckoutScreen
import com.example.duan.View.checkout.ShippingTypeScreen
import com.example.duan.View.home.CategoryProductsScreen
import com.example.duan.View.home.EditAddressScreen
import com.example.duan.View.home.EditProfilePictureScreen
import com.example.duan.View.home.FavoriteScreen
import com.example.duan.View.home.HomeScreen
import com.example.duan.View.home.NotificationsScreen
import com.example.duan.View.home.OrderDetailScreen
import com.example.duan.View.home.OrderHistoryScreen
import com.example.duan.View.home.OrderScreen
import com.example.duan.View.home.PaymentMethodsScreen
import com.example.duan.View.home.PaymentSuccessScreen
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

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    orderViewModel: com.example.duan.ViewModel.OrderViewModel.OrderViewModel = hiltViewModel(),
    moMoResultLauncher: ActivityResultLauncher<Intent>,
    initialIntent: Intent?
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
    val scope = rememberCoroutineScope()

    val userId = authViewModel.getCurrentUserId()
    val userProfile by authViewModel.userProfile.collectAsState()

    // Khởi tạo CartViewModel và lấy cartItems
    val cartViewModel: CartViewModel = hiltViewModel()
    val cartItems by cartViewModel.cartItems.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { cartViewModel.init(it) }
    }

    // BroadcastReceiver để lắng nghe Intent từ MainActivity
    val paymentSuccessReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.duan.PAYMENT_SUCCESS") {
                val userId = intent.getStringExtra("userId") ?: ""
                val paymentId = intent.getStringExtra("paymentId") ?: ""
                val orderId = intent.getStringExtra("orderId") ?: ""
                val totalCost = intent.getDoubleExtra("totalCost", 0.0)

                Log.d("AppNavigation", "Received payment success broadcast: userId=$userId, paymentId=$paymentId, orderId=$orderId, totalCost=$totalCost")
                navController.navigate("payment_success/$userId/$orderId/$totalCost")
            }
        }
    }

    // Đăng ký BroadcastReceiver
    DisposableEffect(Unit) {
        Log.d("AppNavigation", "Registering payment success receiver")
        context.registerReceiver(paymentSuccessReceiver, IntentFilter("com.example.duan.PAYMENT_SUCCESS"))
        onDispose {
            Log.d("AppNavigation", "Unregistering payment success receiver")
            context.unregisterReceiver(paymentSuccessReceiver)
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
            composable(
                route = "favorite/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                if (userId.isNotEmpty()) {
                    FavoriteScreen(
                        userId = userId,
                        navController = navController
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = false }
                        }
                    }
                }
            }
            composable("search") {
                SearchScreen(navController = navController, authViewModel = authViewModel)
            }
            composable("notification") { NotificationsScreen(navController) }
            composable("order") { OrderScreen(navController) }
            composable("track_order/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                TrackOrderScreen(navController = navController, orderId = orderId)
            }
            composable("leave_review/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                LeaveReviewScreen(navController = navController, orderId = orderId)
            }
            composable("setting") { SettingScreen(navController, authViewModel) }
            composable("my_cart") {
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
            composable(
                "checkout/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                LaunchedEffect(userId) {
                    cartViewModel.init(userId)
                }
                CheckoutScreen(
                    userId = userId,
                    cartViewModel = cartViewModel,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
            composable(
                route = "payment_methods/{userId}/{totalCost}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("totalCost") { type = NavType.StringType }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "com.example.duan://paypal/payment_methods/{userId}/{totalCost}"
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val totalCostStr = backStackEntry.arguments?.getString("totalCost") ?: "0.0"
                val totalCost = totalCostStr.toDoubleOrNull() ?: 0.0
                PaymentMethodsScreen(navController, authViewModel, userId, totalCost, moMoResultLauncher)
            }
            composable(
                route = "payment_success/{userId}/{orderId}/{totalCost}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("totalCost") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val totalCost = backStackEntry.arguments?.getString("totalCost")?.toDoubleOrNull() ?: 0.0
                PaymentSuccessScreen(
                    navController = navController,
                    userId = userId,
                    orderId = orderId,
                    totalCost = totalCost
                )
            }
            composable(
                route = "order_details/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderDetailScreen(
                    navController = navController,
                    orderId = orderId
                )
            }
            composable(
                route = "e_receipt/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                // Logic để hiển thị E-Receipt
            }
            composable("shipping_type") {
                ShippingTypeScreen(
                    navController = navController,
                    onShippingSelected = { selectedOption ->
                        val optionMap = mapOf(
                            "id" to selectedOption.id,
                            "name" to selectedOption.name,
                            "estimatedArrival" to selectedOption.estimatedArrival,
                            "deliveryFee" to selectedOption.deliveryFee,
                            "address" to selectedOption.address
                        )
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedShippingOption", optionMap)
                    }
                )
            }
            composable("edit_profile") { EditProfilePictureScreen(navController, authViewModel) }
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