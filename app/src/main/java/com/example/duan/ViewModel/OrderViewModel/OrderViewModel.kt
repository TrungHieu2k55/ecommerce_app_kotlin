package com.example.duan.ViewModel.OrderViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.repository.FirestoreRepository
import com.example.duan.ViewModel.cart.CartViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _reOrderResult = MutableStateFlow<String?>(null)
    val reOrderResult: StateFlow<String?> = _reOrderResult

    private var ordersListener: ListenerRegistration? = null // Quản lý listener

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid
                if (userId == null) {
                    _error.value = "Please log in to view your orders"
                    _isLoading.value = false
                    return@launch
                }

                // Xóa listener cũ nếu tồn tại
                ordersListener?.remove()

                // Sử dụng trường createdAtTimestamp làm Timestamp để sắp xếp
                val query = firestore.collection("orders")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)

                ordersListener = query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _error.value = "Failed to fetch orders: ${error.message}"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val newOrders = snapshot.documents.mapNotNull { doc ->
                            val order = doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                            order
                        }
                        // Loại bỏ trùng lặp dựa trên orderId
                        val uniqueOrders = newOrders.distinctBy { it.orderId }
                        _orders.value = uniqueOrders
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun fetchOrderById(orderId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("orders").document(orderId).get().await()
                if (document.exists()) {
                    val order = document.toObject(Order::class.java)?.copy(orderId = document.id)
                    if (order != null) {
                        val currentOrders = _orders.value.toMutableList()
                        if (!currentOrders.any { it.orderId == orderId }) {
                            currentOrders.add(order)
                            _orders.value = currentOrders
                        } else {
                            val updatedOrders = currentOrders.map {
                                if (it.orderId == orderId) order else it
                            }
                            _orders.value = updatedOrders
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error fetching order: ${e.message}"
            }
        }
    }

    fun submitReviewForProduct(orderId: String, productId: String, rating: Float, comment: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid
                if (userId == null) {
                    _error.value = "Please log in to submit a review"
                    return@launch
                }

                val order = _orders.value.find { it.orderId == orderId }
                if (order == null) {
                    _error.value = "Order not found"
                    return@launch
                }

                val userProfileResult = firestoreRepository.getUserProfile(userId)
                val userProfile = if (userProfileResult.isSuccess) userProfileResult.getOrNull() else null
                val userName = userProfile?.displayName ?: "Unknown User"
                val userAvatar = userProfile?.photoUrl ?: ""

                val newReview = hashMapOf(
                    "orderId" to orderId,
                    "userId" to userId,
                    "rating" to rating,
                    "comment" to comment,
                    "timestamp" to Timestamp.now(),
                    "userName" to userName,
                    "userAvatar" to userAvatar
                )

                firestore.collection("products")
                    .document(productId)
                    .collection("reviews")
                    .document("${orderId}_${userId}")
                    .set(newReview)
                    .await()

                val allItemsReviewed = order.items.all { item ->
                    firestore.collection("products")
                        .document(item.productId)
                        .collection("reviews")
                        .document("${orderId}_${userId}")
                        .get()
                        .await()
                        .exists()
                }

                if (allItemsReviewed) {
                    firestore.collection("orders")
                        .document(orderId)
                        .update("hasReviewed", true)
                        .await()

                    val updatedOrders = _orders.value.map { order ->
                        if (order.orderId == orderId) order.copy(hasReviewed = true) else order
                    }
                    _orders.value = updatedOrders
                }

                Log.d("OrderViewModel", "Review submitted successfully for orderId: $orderId, productId: $productId")
            } catch (e: Exception) {
                _error.value = "Error submitting review: ${e.message}"
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid
                if (userId == null) {
                    _error.value = "Please log in to cancel the order"
                    return@launch
                }

                val order = _orders.value.find { it.orderId == orderId }
                if (order == null) {
                    _error.value = "Order not found"
                    return@launch
                }

                if (order.status == "Delivered" || order.status == "Canceled") {
                    _error.value = "Cannot cancel a delivered or already canceled order"
                    return@launch
                }

                firestoreRepository.updateOrderStatus(orderId, "Canceled")

                val updatedOrders = _orders.value.map { order ->
                    if (order.orderId == orderId) {
                        order.copy(
                            status = "Canceled",
                            canceledAt = Timestamp.now().toString() // Lưu dưới dạng chuỗi
                        )
                    } else order
                }
                _orders.value = updatedOrders

                Log.d("OrderViewModel", "Order canceled successfully for orderId: $orderId")
            } catch (e: Exception) {
                _error.value = "Error canceling order: ${e.message}"
            }
        }
    }

    fun reOrderItems(order: Order, cartViewModel: CartViewModel) {
        viewModelScope.launch {
            try {
                _reOrderResult.value = null
                order.items.forEach { orderItem ->
                    val productResult = firestoreRepository.getProductById(orderItem.productId)
                    if (productResult.isSuccess) {
                        val product = productResult.getOrNull()
                        if (product != null && product.quantityInStock.toInt() >= orderItem.quantity) {
                            cartViewModel.addToCart(
                                item = CartItem(
                                    id = orderItem.productId + System.currentTimeMillis().toString(),
                                    productId = orderItem.productId,
                                    productName = orderItem.name,
                                    image = orderItem.imageUrl,
                                    price = orderItem.price.toDouble(),
                                    quantity = orderItem.quantity
                                )
                            )
                        } else {
                            _reOrderResult.value = "Sản phẩm ${orderItem.name} đã hết hàng!"
                            return@launch
                        }
                    } else {
                        _reOrderResult.value = "Không thể kiểm tra tồn kho!"
                        return@launch
                    }
                }
                _reOrderResult.value = "Đã thêm vào giỏ hàng!"
                cartViewModel.updateCart()
            } catch (e: Exception) {
                _reOrderResult.value = "Lỗi khi thêm vào giỏ hàng: ${e.message}"
            }
        }
    }

    fun resetReOrderResult() {
        _reOrderResult.value = null
    }

    fun refreshOrdersAfterPayment() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid
                if (userId == null) {
                    _error.value = "Please log in to refresh orders"
                    _isLoading.value = false
                    return@launch
                }

                val snapshot = firestore.collection("orders")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ordersList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                }
                // Loại bỏ trùng lặp dựa trên orderId
                _orders.value = ordersList.distinctBy { it.orderId }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error refreshing orders: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        ordersListener?.remove() // Xóa listener khi ViewModel bị hủy
        super.onCleared()
    }
}