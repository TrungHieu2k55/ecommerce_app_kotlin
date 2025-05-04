package com.example.duan.ViewModel.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.api.capturePayPalPayment
import com.example.duan.Model.api.createPayPalPayment
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.repository.FirestoreRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private var userId: String? = null

    fun init(userId: String) {
        if (this.userId != userId) {
            this.userId = userId
            loadCartItems()
        }
    }

    fun loadCartItems() {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                val result = repository.getCartItems(uid)
                if (result.isSuccess) {
                    _cartItems.value = result.getOrNull() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load cart items"
                }
                _isLoading.value = false
            }
        } ?: run {
            _error.value = "User ID not initialized"
        }
    }

    fun addToCart(item: CartItem) {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                val result = repository.addToCart(uid, item)
                if (result.isSuccess) {
                    loadCartItems() // Cập nhật lại danh sách sau khi thêm
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to add to cart"
                }
                _isLoading.value = false
            }
        }
    }

    fun removeFromCart(itemId: String) {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                val result = repository.deleteCartItem(uid, itemId)
                if (result.isSuccess) {
                    loadCartItems() // Cập nhật lại danh sách sau khi xóa
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to remove from cart"
                }
                _isLoading.value = false
            }
        }
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                val result = repository.updateCartItemQuantity(uid, itemId, newQuantity)
                if (result.isSuccess) {
                    loadCartItems() // Cập nhật lại danh sách sau khi cập nhật
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to update quantity"
                }
                _isLoading.value = false
            }
        }
    }

    fun clearCart() {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                val result = repository.clearCart(uid)
                if (result.isSuccess) {
                    _cartItems.value = emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to clear cart"
                }
                _isLoading.value = false
            }
        }
    }

    fun setDiscount(amount: Double) {
        viewModelScope.launch {
            _discount.value = amount
        }
    }

    // Hàm xử lý thanh toán PayPal
    fun processPayPalPayment(totalCost: Double, onApprovalUrl: (String?) -> Unit) {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                try {
                    val approvalUrl = createPayPalPayment(uid, totalCost)
                    onApprovalUrl(approvalUrl)
                } catch (e: Exception) {
                    Log.e("CartViewModel", "Error creating PayPal payment: $e")
                    _error.value = "Failed to create PayPal payment: ${e.message}"
                    onApprovalUrl(null)
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: run {
            _error.value = "User ID not initialized"
            onApprovalUrl(null)
        }
    }

    // Hàm xác nhận thanh toán và cập nhật trạng thái đơn hàng
    fun confirmPayPalPayment(orderId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val captureResponse = capturePayPalPayment(orderId)
                if (captureResponse?.status == "COMPLETED") {
                    // Cập nhật trạng thái đơn hàng trong Firestore
                    userId?.let { uid ->
                        val orderRef = Firebase.firestore.collection("users")
                            .document(uid)
                            .collection("orders")
                            .document(orderId)
                        orderRef.update(
                            mapOf(
                                "status" to "Completed",
                                "paymentId" to captureResponse.id,
                                "paymentTime" to com.google.firebase.Timestamp.now()
                            )
                        ).addOnSuccessListener {
                            onResult(true, orderId)
                        }.addOnFailureListener { e ->
                            Log.e("CartViewModel", "Error updating order status: $e")
                            _error.value = "Failed to update order status: ${e.message}"
                            onResult(false, null)
                        }
                    } ?: run {
                        _error.value = "User ID not initialized"
                        onResult(false, null)
                    }
                } else {
                    _error.value = "Payment capture failed: ${captureResponse?.status}"
                    onResult(false, null)
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error capturing PayPal payment: $e")
                _error.value = "Failed to capture PayPal payment: ${e.message}"
                onResult(false, null)
            } finally {
                _isLoading.value = false
            }
        }
    }
}