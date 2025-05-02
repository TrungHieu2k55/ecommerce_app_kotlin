package com.example.duan.ViewModel.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.repository.FirestoreRepository
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

    private lateinit var userId: String

    fun init(userId: String) {
        this.userId = userId
        loadCartItems()
    }

    fun loadCartItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.getCartItems(userId)
            if (result.isSuccess) {
                _cartItems.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load cart items"
            }
            _isLoading.value = false
        }
    }

    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.addToCart(userId, item)
            if (result.isSuccess) {
                loadCartItems() // Cập nhật lại danh sách sau khi thêm
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to add to cart"
            }
            _isLoading.value = false
        }
    }

    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.deleteCartItem(userId, itemId)
            if (result.isSuccess) {
                loadCartItems() // Cập nhật lại danh sách sau khi xóa
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to remove from cart"
            }
            _isLoading.value = false
        }
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.updateCartItemQuantity(userId, itemId, newQuantity)
            if (result.isSuccess) {
                loadCartItems() // Cập nhật lại danh sách sau khi cập nhật
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update quantity"
            }
            _isLoading.value = false
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.clearCart(userId)
            if (result.isSuccess) {
                _cartItems.value = emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to clear cart"
            }
            _isLoading.value = false
        }
    }
}