package com.example.duan.ViewModel.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.api.capturePayPalPayment
import com.example.duan.Model.api.createPayPalPayment
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.repository.CartRepository
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
    private val cartRepository: CartRepository,
    private val firestoreRepository: FirestoreRepository
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
    private var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun init(userId: String) {
        if (this.userId != userId) {
            this.userId = userId
            setupFirestoreListener()
        }
    }

    private fun setupFirestoreListener() {
        userId?.let { uid ->
            _isLoading.value = true
            _error.value = null
            firestoreListener?.remove() // Xóa listener cũ nếu có
            firestoreListener = Firebase.firestore.collection("users")
                .document(uid)
                .collection("cart")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _error.value = "Failed to load cart items: ${error.message}"
                        _isLoading.value = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.e("CartViewModel", "Error parsing cart item: ${e.message}")
                                null
                            }
                        }
                        _cartItems.value = items
                        _isLoading.value = false
                        Log.d("CartViewModel", "Cart updated for userId: $uid, items: $items")
                    }
                }
        } ?: run {
            _error.value = "User ID not initialized"
            _isLoading.value = false
        }
    }

    fun addToCart(item: CartItem) {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                // Cập nhật tạm thời _cartItems để giao diện phản ánh ngay
                val tempItem = item.copy(id = "temp_${System.currentTimeMillis()}")
                val currentItems = _cartItems.value.toMutableList()
                currentItems.add(tempItem)
                _cartItems.value = currentItems

                val result = cartRepository.addToCart(item)
                if (result.isSuccess) {
                    // Không cần gọi loadCartItems vì snapshot listener sẽ cập nhật
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to add to cart"
                    // Hoàn tác cập nhật tạm thời nếu Firestore thất bại
                    _cartItems.value = currentItems.filter { it.id != tempItem.id }
                }
                _isLoading.value = false
            }
        } ?: run {
            _error.value = "User ID not initialized"
        }
    }

    fun removeFromCart(itemId: String) {
        userId?.let { uid ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                val result = cartRepository.removeFromCart(itemId)
                if (!result.isSuccess) {
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
                val item = _cartItems.value.find { it.id == itemId }?.copy(quantity = newQuantity)
                if (item != null) {
                    val result = cartRepository.updateCartItem(itemId, item)
                    if (!result.isSuccess) {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to update quantity"
                    }
                } else {
                    _error.value = "Item not found"
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
                val result = cartRepository.clearCart(uid)
                if (result.isSuccess) {
                    _cartItems.value = emptyList()
                    Log.d("CartViewModel", "Cart cleared successfully for userId: $uid")
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

    fun updateCart() {
        setupFirestoreListener() // Làm mới dữ liệu giỏ hàng từ Firestore
    }

    suspend fun validateCartItems(): Boolean {
        try {
            _isLoading.value = true
            _error.value = null
            val items = _cartItems.value
            for (item in items) {
                val productResult = firestoreRepository.getProductById(item.productId)
                if (productResult.isFailure) {
                    _error.value = "Không thể tải thông tin sản phẩm ${item.productName}"
                    Log.e("CartViewModel", "Failed to load product ${item.productId}")
                    return false
                }
                val product = productResult.getOrNull()
                if (product == null || product.quantityInStock .toInt()< item.quantity) {
                    _error.value = "Sản phẩm ${item.productName} đã hết hàng hoặc không đủ số lượng (có: ${product?.quantityInStock}, cần: ${item.quantity})"
                    Log.e("CartViewModel", "Invalid product ${item.productName}: available=${product?.quantityInStock}, requested=${item.quantity}")
                    return false
                }
            }
            _error.value = null
            return true
        } catch (e: Exception) {
            _error.value = e.message ?: "Lỗi khi kiểm tra giỏ hàng"
            Log.e("CartViewModel", "Error validating cart: ${e.message}", e)
            return false
        } finally {
            _isLoading.value = false
        }
    }

    fun removeInvalidItems() {
        userId?.let { uid ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _error.value = null
                    val validItems = _cartItems.value.filter { item ->
                        val productResult = firestoreRepository.getProductById(item.productId)
                        val product = productResult.getOrNull()
                        product != null && product.quantityInStock.toInt() >= item.quantity
                    }
                    if (validItems.size < _cartItems.value.size) {
                        _error.value = "Đã xóa các sản phẩm hết hàng khỏi giỏ hàng"
                        Log.d("CartViewModel", "Removed invalid items, new cart: $validItems")
                    }
                    _cartItems.value = validItems
                    // Cập nhật giỏ hàng trong Firestore
                    val result = cartRepository.clearCart(uid)
                    if (result.isSuccess) {
                        validItems.forEach { item ->
                            cartRepository.addToCart(item)
                        }
                        Log.d("CartViewModel", "Updated cart in Firestore for userId: $uid")
                    } else {
                        _error.value = "Lỗi khi cập nhật giỏ hàng: ${result.exceptionOrNull()?.message}"
                        Log.e("CartViewModel", "Error updating cart: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    _error.value = "Lỗi khi cập nhật giỏ hàng: ${e.message}"
                    Log.e("CartViewModel", "Error updating cart: ${e.message}", e)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun confirmPayPalPayment(orderId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val captureResponse = capturePayPalPayment(orderId)
                if (captureResponse?.status == "COMPLETED") {
                    Log.d("CartViewModel", "PayPal payment captured successfully: ${captureResponse.id}")
                    userId?.let { uid ->
                        val orderRef = Firebase.firestore.collection("orders")
                            .document(orderId)
                        orderRef.update(
                            mapOf(
                                "status" to "Delivered",
                                "paymentId" to captureResponse.id,
                                "paymentTime" to com.google.firebase.Timestamp.now()
                            )
                        ).addOnSuccessListener {
                            Log.d("CartViewModel", "Order status updated successfully for orderId: $orderId")
                            clearCart()
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

    override fun onCleared() {
        firestoreListener?.remove()
        super.onCleared()
    }
}