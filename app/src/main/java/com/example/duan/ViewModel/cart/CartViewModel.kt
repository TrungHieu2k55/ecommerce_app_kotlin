package com.example.duan.ViewModel.cart

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    val cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val error = mutableStateOf<String?>(null)



    fun loadCartItems(userId: String) {
        viewModelScope.launch {
            error.value = null
            val result = repository.getCartItems(userId)
            if (result.isSuccess) {
                cartItems.value = result.getOrNull() ?: emptyList()
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Failed to load cart items"
            }
        }
    }

    fun addToCart(userId: String, item: CartItem) {
        viewModelScope.launch {
            error.value = null
            val result = repository.addToCart(userId, item)
            if (result.isSuccess) {
                loadCartItems(userId) // Cập nhật lại danh sách sau khi thêm
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Failed to add to cart"
            }
        }
    }

    fun removeFromCart(userId: String, item: CartItem) {
        viewModelScope.launch {
            error.value = null
            val result = repository.deleteCartItem(userId, item.id)
            if (result.isSuccess) {
                loadCartItems(userId) // Cập nhật lại danh sách sau khi xóa
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Failed to remove from cart"
            }
        }
    }

    fun updateQuantity(userId: String?, item: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            error.value = null
            val result = userId?.let { repository.updateCartItemQuantity(it, item.id, newQuantity) }
            if (result!!.isSuccess) {
                loadCartItems(userId) // Cập nhật lại danh sách sau khi cập nhật
            } else {
                error.value = result.exceptionOrNull()?.message ?: "Failed to update quantity"
            }
        }
    }
}