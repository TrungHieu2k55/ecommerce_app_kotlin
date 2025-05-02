package com.example.duan.ViewModel.usecase.product

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
import android.util.Log

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    // Chuyển các biến trạng thái sang StateFlow
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _topSellingProducts = MutableStateFlow<List<Product>>(emptyList())
    val topSellingProducts: StateFlow<List<Product>> = _topSellingProducts.asStateFlow()

    private val _newInProducts = MutableStateFlow<List<Product>>(emptyList())
    val newInProducts: StateFlow<List<Product>> = _newInProducts.asStateFlow()

    private val _hoodiesProducts = MutableStateFlow<List<Product>>(emptyList())
    val hoodiesProducts: StateFlow<List<Product>> = _hoodiesProducts.asStateFlow()

    private val _trendingProducts = MutableStateFlow<List<Product>>(emptyList())
    val trendingProducts: StateFlow<List<Product>> = _trendingProducts.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _technologyProducts = MutableStateFlow<List<Product>>(emptyList())
    val technologyProducts: StateFlow<List<Product>> = _technologyProducts.asStateFlow()

    private val _categoryProducts = MutableStateFlow<List<Product>>(emptyList())
    val categoryProducts: StateFlow<List<Product>> = _categoryProducts.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // Khởi tạo để lấy tất cả sản phẩm khi ViewModel được tạo
    init {
        fetchAllProducts()
    }

    fun fetchAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getAllProducts()
            if (result.isSuccess) {
                _products.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load products"
            }
            _isLoading.value = false
        }
    }

    fun fetchTopSellingProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getTopSellingProducts()
            if (result.isSuccess) {
                _topSellingProducts.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load top selling products"
            }
            _isLoading.value = false
        }
    }

    fun fetchNewInProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getNewInProducts()
            if (result.isSuccess) {
                _newInProducts.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load new in products"
            }
            _isLoading.value = false
        }
    }

    fun fetchHoodiesProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getHoodiesProducts()
            if (result.isSuccess) {
                _hoodiesProducts.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load hoodies products"
            }
            _isLoading.value = false
        }
    }

    fun fetchTrendingProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getTrendingProducts()
            if (result.isSuccess) {
                _trendingProducts.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load trending products"
            }
            _isLoading.value = false
        }
    }

    fun fetchReviews(productId: String) {
        viewModelScope.launch {
            val result = repository.getReviews(productId)
            if (result.isSuccess) {
                _reviews.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load reviews"
            }
        }
    }

    fun fetchProductById(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getProductById(productId)
            if (result.isSuccess) {
                _selectedProduct.value = result.getOrNull()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load product"
                _selectedProduct.value = null
            }
            _isLoading.value = false
        }
    }

    fun fetchTechnologyProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getProductsByCategory("technology")
            if (result.isSuccess) {
                _technologyProducts.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load technology products"
            }
            _isLoading.value = false
        }
    }

    fun fetchProductsByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d("ProductViewModel", "Fetching products for category: $category")
            val result = repository.getProductsByCategory(category)
            if (result.isSuccess) {
                _categoryProducts.value = result.getOrNull() ?: emptyList()
                Log.d("ProductViewModel", "Products fetched: ${_categoryProducts.value.size}")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load products for category $category"
                Log.e("ProductViewModel", "Error: ${_errorMessage.value}")
            }
            _isLoading.value = false
        }
    }
}