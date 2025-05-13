package com.example.duan.Model.repository

import android.util.Log
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.model.Order
import com.example.duan.Model.model.Product
import com.example.duan.Model.model.Review
import com.example.duan.Model.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class FirestoreRepository @Inject constructor() {
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
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
                val data = doc.data
                val quantityInStock = data?.get("quantityInStock")?.let {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull() ?: 0
                        else -> 0
                    }
                } ?: 0
                doc.toObject(Product::class.java)?.copy(
                    id = doc.id,
                    quantityInStock = quantityInStock
                )
            }
            Result.success(products)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching products", e)
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
                val data = doc.data
                val quantityInStock = data?.get("quantityInStock")?.let {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull() ?: 0
                        else -> 0
                    }
                } ?: 0
                doc.toObject(Product::class.java)?.copy(
                    id = doc.id,
                    quantityInStock = quantityInStock
                )
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
                val data = doc.data
                val quantityInStock = data?.get("quantityInStock")?.let {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull() ?: 0
                        else -> 0
                    }
                } ?: 0
                doc.toObject(Product::class.java)?.copy(
                    id = doc.id,
                    quantityInStock = quantityInStock
                )
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
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                val quantityInStock = data?.get("quantityInStock")?.let {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull() ?: 0
                        else -> 0
                    }
                } ?: 0
                doc.toObject(Product::class.java)?.copy(
                    id = doc.id,
                    quantityInStock = quantityInStock
                )
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
                .whereEqualTo("categories", "Hoodies")
                .limit(limit)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                val quantityInStock = data?.get("quantityInStock")?.let {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull() ?: 0
                        else -> 0
                    }
                } ?: 0
                doc.toObject(Product::class.java)?.copy(
                    id = doc.id,
                    quantityInStock = quantityInStock
                )
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

    suspend fun getProductsByCategory(categories: String): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("categories", categories)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                val quantityInStock = data?.get("quantityInStock")?.let {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull() ?: 0
                        else -> 0
                    }
                } ?: 0
                Log.d("FirestoreRepository", "Product ${doc.id}: quantityInStock raw=${data?.get("quantityInStock")}, converted=$quantityInStock")
                doc.toObject(Product::class.java)?.copy(
                    id = doc.id,
                    quantityInStock = quantityInStock
                )
            }
            Result.success(products)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching products by category", e)
            Result.failure(e)
        }
    }

    // Xóa toàn bộ giỏ hàng sau khi đặt hàng thành công
    suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .await()
            val batch = firestore.batch()
            snapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveOrder(order: Order) {
        try {
            val tempLockRef = firestore.collection("temp_locks").document(order.orderId)
            firestore.runTransaction { transaction ->
                val orderRef = firestore.collection("orders").document(order.orderId)

                // Kiểm tra khóa tạm thời
                val lockSnapshot = transaction.get(tempLockRef)
                if (lockSnapshot.exists()) {
                    Log.d("FirestoreRepository", "Đơn hàng đang được xử lý, bỏ qua: ${order.orderId}")
                    return@runTransaction
                }

                // Kiểm tra xem đơn hàng đã tồn tại chưa
                val snapshot = transaction.get(orderRef)
                if (snapshot.exists()) {
                    Log.d("FirestoreRepository", "Đơn hàng với orderId ${order.orderId} đã tồn tại, bỏ qua.")
                    return@runTransaction
                }

                // Kiểm tra dữ liệu sản phẩm
                val productSnapshots = order.items.map { item ->
                    val productRef = firestore.collection("products").document(item.productId)
                    item to transaction.get(productRef)
                }

                // Xác thực từng sản phẩm
                productSnapshots.forEach { (item, productSnapshot) ->
                    if (!productSnapshot.exists()) {
                        Log.e("FirestoreRepository", "Sản phẩm với ID ${item.productId} không tồn tại")
                        throw Exception("Sản phẩm với ID ${item.productId} không tồn tại")
                    }
                    val product = productSnapshot.toObject(Product::class.java)

                    // Kiểm tra số lượng tồn kho
                    Log.d("FirestoreRepository", "Kiểm tra sản phẩm ${item.name}: có sẵn=${product?.quantityInStock}, yêu cầu=${item.quantity}")
                    if (product == null || product.quantityInStock < item.quantity) {
                        throw Exception("Sản phẩm ${item.name} hết hàng hoặc không đủ số lượng (có sẵn: ${product?.quantityInStock}, yêu cầu: ${item.quantity})")
                    }
                }

                // Cập nhật số lượng sản phẩm
                productSnapshots.forEach { (item, _) ->
                    val productRef = firestore.collection("products").document(item.productId)
                    // Giảm số lượng tồn kho
                    transaction.update(productRef, "quantityInStock", FieldValue.increment(-item.quantity.toLong()))
                    // Tăng số lượng đã bán
                    transaction.update(productRef, "quantitySold", FieldValue.increment(item.quantity.toLong()))
                }

                // Tạo timestamp và lưu đơn hàng
                val currentTimestamp = Timestamp.now()
                val orderWithTimestamp = order.copy(
                    // Sử dụng hàm mở rộng để format timestamp
                    createdAt = currentTimestamp.toFormattedDateTime(),
                )

                // Thiết lập khóa tạm thời và lưu đơn hàng
                Log.d("FirestoreRepository", "Lưu đơn hàng với ID tài liệu: ${order.orderId}, userId: ${order.userId}, dữ liệu: $orderWithTimestamp")
                transaction.set(tempLockRef, mapOf("createdAt" to currentTimestamp))
                transaction.set(orderRef, orderWithTimestamp)
            }.await()

            // Xóa khóa tạm thời
            tempLockRef.delete().await()

            // Xóa giỏ hàng sau khi lưu thành công
            clearCart(order.userId)
            Log.d("FirestoreRepository", "Lưu đơn hàng thành công với ID tài liệu: ${order.orderId}")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Lỗi khi lưu đơn hàng: ${e.message}", e)
            throw e
        }
    }

    // Cập nhật trạng thái đơn hàng
    suspend fun updateOrderStatus(orderId: String, status: String) {
        if (orderId.isEmpty()) {
            throw IllegalArgumentException("orderId không được để trống")
        }
        try {
            // Tạo timestamp và cập nhật trạng thái
            val currentTimestamp = Timestamp.now()
            val updates = hashMapOf(
                "status" to status,
                "${status.lowercase()}_at" to currentTimestamp.toFormattedDateTime()
            )

            firestore.collection("orders").document(orderId)
                .update(updates as Map<String, Any>)
                .await()
            Log.d("FirestoreRepository", "Đã cập nhật trạng thái đơn hàng cho $orderId thành $status")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Không thể cập nhật trạng thái đơn hàng cho $orderId: ${e.message}")
            throw e
        }
    }

    // Tăng lượt xem sản phẩm
    suspend fun incrementViews(productId: String): Result<Unit> {
        return try {
            firestore.collection("products")
                .document(productId)
                .update("views", FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm một sản phẩm vào danh sách yêu thích của người dùng
    suspend fun addFavorite(userId: String, productId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(productId)
                .set(mapOf("productId" to productId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Xóa một sản phẩm khỏi danh sách yêu thích của người dùng
    suspend fun removeFavorite(userId: String, productId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(productId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy tất cả các sản phẩm được yêu thích của người dùng
    suspend fun getFavorites(userId: String): Result<List<Product>> {
        return try {
            val favoriteDocs = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            val productIds = favoriteDocs.documents.mapNotNull { it.getString("productId") }
            val products = mutableListOf<Product>()

            for (productId in productIds) {
                val productDoc = firestore.collection("products")
                    .document(productId)
                    .get()
                    .await()
                val product = productDoc.toObject(Product::class.java)?.copy(id = productDoc.id)
                if (product != null) {
                    products.add(product)
                }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kiểm tra xem một sản phẩm có được yêu thích bởi người dùng không
    suspend fun isFavorited(userId: String, productId: String): Result<Boolean> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(productId)
                .get()
                .await()
            Result.success(doc.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm mở rộng để chuyển đổi Timestamp sang chuỗi ngày giờ có định dạng
    fun Timestamp.toFormattedDateTime(format: String = "dd/MM/yyyy HH:mm:ss"): String {
        return try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault() // Sử dụng múi giờ mặc định của thiết bị
            sdf.format(this.toDate())
        } catch (e: Exception) {
            // Quay về toString nếu việc định dạng thất bại
            this.toString()
        }
    }
}