package com.example.duan.ViewModel.usecase.product

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.model.Product
import com.example.duan.Model.model.Review
import com.example.duan.Model.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    val products = mutableStateOf<List<Product>>(emptyList())
    val topSellingProducts = mutableStateOf<List<Product>>(emptyList())
    val newInProducts = mutableStateOf<List<Product>>(emptyList())
    val hoodiesProducts = mutableStateOf<List<Product>>(emptyList())
    val trendingProducts = mutableStateOf<List<Product>>(emptyList())
    val reviews = mutableStateOf<List<Review>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    fun fetchAllProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = repository.getAllProducts()
            if (result.isSuccess) {
                products.value = result.getOrNull() ?: emptyList()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load products"
            }
            isLoading.value = false
        }
    }

    fun fetchTopSellingProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = repository.getTopSellingProducts()
            if (result.isSuccess) {
                topSellingProducts.value = result.getOrNull() ?: emptyList()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load top selling products"
            }
            isLoading.value = false
        }
    }

    fun fetchNewInProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = repository.getNewInProducts()
            if (result.isSuccess) {
                newInProducts.value = result.getOrNull() ?: emptyList()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load new in products"
            }
            isLoading.value = false
        }
    }

    fun fetchHoodiesProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = repository.getHoodiesProducts()
            if (result.isSuccess) {
                hoodiesProducts.value = result.getOrNull() ?: emptyList()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load hoodies products"
            }
            isLoading.value = false
        }
    }

    fun fetchTrendingProducts() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = repository.getTrendingProducts()
            if (result.isSuccess) {
                trendingProducts.value = result.getOrNull() ?: emptyList()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load trending products"
            }
            isLoading.value = false
        }
    }

    fun fetchReviews(productId: String) {
        viewModelScope.launch {
            val result = repository.getReviews(productId)
            if (result.isSuccess) {
                reviews.value = result.getOrNull() ?: emptyList()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load reviews"
            }
        }
    }

    fun fetchProductById(productId: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = repository.getProductById(productId) // Cần thêm hàm này trong FirestoreRepository
            if (result.isSuccess) {
                _selectedProduct.value = result.getOrNull()
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load product"
                _selectedProduct.value = null
            }
            isLoading.value = false
        }
    }
}