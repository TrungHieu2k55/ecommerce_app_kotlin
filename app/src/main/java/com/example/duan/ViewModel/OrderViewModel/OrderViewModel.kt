package com.example.duan.ViewModel.OrderViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

@HiltViewModel
class OrderViewModel @Inject constructor() : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        Log.d("OrderViewModel", "ViewModel initialized, fetching orders")
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "Starting fetchOrders")
                _isLoading.value = true
                _error.value = null

                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid
                if (userId == null) {
                    Log.e("OrderViewModel", "User not logged in")
                    _error.value = "Please log in to view your orders"
                    _isLoading.value = false
                    return@launch
                }
                Log.d("OrderViewModel", "Fetching orders for userId = $userId")

                firestore.collection("orders")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("OrderViewModel", "Failed to fetch orders: ${error.message}")
                            _error.value = "Failed to fetch orders: ${error.message}"
                            _isLoading.value = false
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val ordersList = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                            }
                            Log.d("OrderViewModel", "Fetched orders successfully: $ordersList")
                            _orders.value = ordersList
                            _isLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Exception during fetchOrders: ${e.message}")
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun fetchOrderById(orderId: String) {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "Fetching order by ID: $orderId")
                val document = firestore.collection("orders").document(orderId).get().await()
                if (document.exists()) {
                    val order = document.toObject(Order::class.java)?.copy(orderId = document.id)
                    if (order != null) {
                        Log.d("OrderViewModel", "Order found: $order")
                        val currentOrders = _orders.value.toMutableList()
                        if (!currentOrders.any { it.orderId == orderId }) {
                            currentOrders.add(order)
                            _orders.value = currentOrders.sortedByDescending { it.createdAt?.seconds }
                            Log.d("OrderViewModel", "Updated orders list: ${_orders.value}")
                        }
                    } else {
                        Log.d("OrderViewModel", "Order not found for ID: $orderId")
                    }
                } else {
                    Log.d("OrderViewModel", "Document does not exist for orderId: $orderId")
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error fetching order by ID: ${e.message}")
                _error.value = "Error fetching order: ${e.message}"
            }
        }
    }

    fun submitReview(orderId: String, rating: Float, comment: String) {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "Submitting review for orderId: $orderId")
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid
                if (userId == null) {
                    Log.e("OrderViewModel", "User not logged in")
                    _error.value = "Please log in to submit a review"
                    return@launch
                }

                val reviewData = hashMapOf(
                    "orderId" to orderId,
                    "userId" to userId,
                    "rating" to rating,
                    "comment" to comment,
                    "createdAt" to Timestamp.now()
                )

                firestore.collection("reviews")
                    .document("${orderId}_${userId}")
                    .set(reviewData)
                    .await()

                Log.d("OrderViewModel", "Review submitted successfully for orderId: $orderId")
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error submitting review: ${e.message}")
                _error.value = "Error submitting review: ${e.message}"
            }
        }
    }

    fun refreshOrdersAfterPayment() {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "Refreshing orders after payment")
                _isLoading.value = true
                _error.value = null

                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid
                if (userId == null) {
                    Log.e("OrderViewModel", "User not logged in")
                    _error.value = "Please log in to refresh orders"
                    _isLoading.value = false
                    return@launch
                }
                Log.d("OrderViewModel", "Refreshing orders for userId = $userId")

                val snapshot = firestore.collection("orders")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ordersList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                }
                Log.d("OrderViewModel", "Refreshed orders successfully: $ordersList")
                _orders.value = ordersList
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error refreshing orders: ${e.message}")
                _error.value = "Error refreshing orders: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}