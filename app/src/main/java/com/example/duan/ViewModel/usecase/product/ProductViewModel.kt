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
import android.util.Log
import javax.inject.Inject

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

    // StateFlow mới cho các sản phẩm được yêu thích
    private val _favoritedProducts = MutableStateFlow<List<Product>>(emptyList())
    val favoritedProducts: StateFlow<List<Product>> = _favoritedProducts.asStateFlow()

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
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} products")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load products"
                Log.e("ProductViewModel", "Error fetching products: ${_errorMessage.value}")
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
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} top selling products")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load top selling products"
                Log.e("ProductViewModel", "Error fetching top selling products: ${_errorMessage.value}")
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
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} new in products")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load new in products"
                Log.e("ProductViewModel", "Error fetching new in products: ${_errorMessage.value}")
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
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} hoodies products")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load hoodies products"
                Log.e("ProductViewModel", "Error fetching hoodies products: ${_errorMessage.value}")
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
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} trending products")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load trending products"
                Log.e("ProductViewModel", "Error fetching trending products: ${_errorMessage.value}")
            }
            _isLoading.value = false
        }
    }

    fun fetchReviews(productId: String) {
        viewModelScope.launch {
            val result = repository.getReviews(productId)
            if (result.isSuccess) {
                _reviews.value = result.getOrNull() ?: emptyList()
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} reviews for product $productId")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load reviews"
                Log.e("ProductViewModel", "Error fetching reviews: ${_errorMessage.value}")
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
                Log.d("ProductViewModel", "Fetched product with id: $productId")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load product"
                _selectedProduct.value = null
                Log.e("ProductViewModel", "Error fetching product: ${_errorMessage.value}")
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
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} technology products")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load technology products"
                Log.e("ProductViewModel", "Error fetching technology products: ${_errorMessage.value}")
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
                Log.d("ProductViewModel", "Fetched ${result.getOrNull()?.size} products for category $category")
                result.getOrNull()?.forEach { product ->
                    Log.d("ProductViewModel", "Product ${product.id}: quantityInStock = ${product.quantityInStock}")
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load products for category $category"
                Log.e("ProductViewModel", "Error fetching products for category $category: ${_errorMessage.value}")
            }
            _isLoading.value = false
        }
    }

    // Thêm phương thức để tăng lượt xem
    fun incrementProductViews(productId: String) {
        viewModelScope.launch {
            val result = repository.incrementViews(productId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to increment views"
                Log.e("ProductViewModel", "Error incrementing views: ${_errorMessage.value}")
            } else {
                Log.d("ProductViewModel", "Incremented views for product: $productId")
            }
        }
    }

    // Các phương thức mới cho danh sách yêu thích
    fun fetchFavoritedProducts(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getFavorites(userId)
            if (result.isSuccess) {
                _favoritedProducts.value = result.getOrNull() ?: emptyList()
                Log.d("ProductViewModel", "Đã lấy ${result.getOrNull()?.size} sản phẩm yêu thích cho người dùng $userId")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Không thể tải sản phẩm yêu thích"
                Log.e("ProductViewModel", "Lỗi khi lấy sản phẩm yêu thích: ${_errorMessage.value}")
            }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(userId: String, product: Product) {
        viewModelScope.launch {
            val isCurrentlyFavorited = repository.isFavorited(userId, product.id).getOrNull() ?: false
            if (isCurrentlyFavorited) {
                val result = repository.removeFavorite(userId, product.id)
                if (result.isSuccess) {
                    _favoritedProducts.value = _favoritedProducts.value.filter { it.id != product.id }
                    Log.d("ProductViewModel", "Đã xóa sản phẩm ${product.id} khỏi danh sách yêu thích")
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Không thể xóa khỏi danh sách yêu thích"
                    Log.e("ProductViewModel", "Lỗi khi xóa khỏi danh sách yêu thích: ${_errorMessage.value}")
                }
            } else {
                val result = repository.addFavorite(userId, product.id)
                if (result.isSuccess) {
                    _favoritedProducts.value = _favoritedProducts.value + product
                    Log.d("ProductViewModel", "Đã thêm sản phẩm ${product.id} vào danh sách yêu thích")
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Không thể thêm vào danh sách yêu thích"
                    Log.e("ProductViewModel", "Lỗi khi thêm vào danh sách yêu thích: ${_errorMessage.value}")
                }
            }
        }
    }

    suspend fun isProductFavorited(userId: String, productId: String): Boolean {
        return repository.isFavorited(userId, productId).getOrNull() ?: false
    }
}