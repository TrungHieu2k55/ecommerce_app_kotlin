package com.example.duan.Model.repository

import android.util.Log
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.model.Order
import com.example.duan.Model.model.Product
import com.example.duan.Model.model.Review
import com.example.duan.Model.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor() {
    internal val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Lấy thông tin người dùng
    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val userProfile = document.toObject(UserProfile::class.java)?.copy(uid = document.id)

            if (document.exists()) {
                Log.d("Firestore", "Document data: ${document.data}")
                Log.d("Firestore", "UserProfile: $userProfile")
            } else {
                Log.d("Firestore", "Document does not exist for userId: $userId")
            }

            Result.success(userProfile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật địa chỉ người dùng
    suspend fun updateUserAddress(userId: String, address: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("address", address)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật phương thức thanh toán
    suspend fun updatePaymentMethods(userId: String, paymentMethods: List<String>): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("paymentMethods", paymentMethods)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật ảnh đại diện
    suspend fun updateProfilePicture(userId: String, photoUrl: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("photoUrl", photoUrl)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy lịch sử đơn hàng
    suspend fun getOrderHistory(userId: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(orderId = doc.id)
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm sản phẩm
    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            firestore.collection("products")
                .add(product)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy tất cả sản phẩm
    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy sản phẩm bán chạy
    suspend fun getTopSellingProducts(limit: Long = 5): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .orderBy("quantitySold", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy sản phẩm mới
    suspend fun getNewInProducts(limit: Long = 5): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy sản phẩm xu hướng
    suspend fun getTrendingProducts(limit: Long = 5): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .orderBy("addedToCartCount", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy sản phẩm hoodie
    suspend fun getHoodiesProducts(limit: Long = 5): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("category", "Hoodies")
                .limit(limit)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy sản phẩm theo ID
    suspend fun getProductById(productId: String): Result<Product?> {
        return try {
            val document = firestore.collection("products")
                .document(productId)
                .get()
                .await()
            val product = document.toObject(Product::class.java)?.copy(id = document.id)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy đánh giá sản phẩm
    suspend fun getReviews(productId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection("products")
                .document(productId)
                .collection("reviews")
                .get()
                .await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)
            }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm sản phẩm vào giỏ hàng
    suspend fun addToCart(userId: String, cartItem: CartItem): Result<Unit> {
        return try {
            firestore.collection("products")
                .document(cartItem.productId)
                .update("addedToCartCount", FieldValue.increment(1))
                .await()

            val cartItemMap = hashMapOf(
                "productId" to cartItem.productId,
                "productName" to cartItem.productName,
                "price" to cartItem.price,
                "quantity" to cartItem.quantity,
                "size" to cartItem.size,
                "color" to cartItem.color,
                "image" to cartItem.image,
                "addedAt" to Timestamp.now()
            )
            firestore.collection("users")
                .document(userId)
                .collection("cart")
                .add(cartItemMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy danh sách sản phẩm trong giỏ hàng
    suspend fun getCartItems(userId: String): Result<List<CartItem>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .await()
            val cartItems = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CartItem::class.java)?.copy(id = doc.id)
            }
            Result.success(cartItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa sản phẩm khỏi giỏ hàng
    suspend fun deleteCartItem(userId: String, cartItemId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("cart")
                .document(cartItemId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật số lượng sản phẩm trong giỏ
    suspend fun updateCartItemQuantity(userId: String, cartItemId: String, newQuantity: Int): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("cart")
                .document(cartItemId)
                .update("quantity", newQuantity)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật số lượng sản phẩm đã bán
    suspend fun incrementQuantitySold(productId: String, quantity: Int): Result<Unit> {
        return try {
            firestore.collection("products")
                .document(productId)
                .update("quantitySold", FieldValue.increment(quantity.toLong()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}