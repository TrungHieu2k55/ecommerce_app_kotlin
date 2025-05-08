package com.example.duan.Model.repository

import android.util.Log
import com.example.duan.Model.model.CartItem
import com.example.duan.Model.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private suspend fun ensureUserDocumentExists(userId: String) {
        val userDoc = db.collection("users").document(userId).get().await()
        if (!userDoc.exists()) {
            val userProfile = UserProfile(
                uid = userId,
                displayName = auth.currentUser?.displayName ?: "",
                email = auth.currentUser?.email ?: "",
                phoneNumber = "",
                addresses = emptyList(),
                selectedAddress = null,
                paymentMethods = emptyList(),
                photoUrl = auth.currentUser?.photoUrl?.toString()
            )
            db.collection("users").document(userId)
                .set(userProfile, SetOptions.merge())
                .await()
            Log.d("CartRepository", "Created user document for user: $userId")
        }
    }

    suspend fun addToCart(item: CartItem): Result<String> {
        return try {
            val userId = currentUserId ?: throw Exception("User not logged in")
            ensureUserDocumentExists(userId)
            val docRef = db.collection("users")
                .document(userId)
                .collection("cart")
                .add(item)
                .await()
            Log.d("CartRepository", "Added item to cart with ID: ${docRef.id} for user: $userId")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("CartRepository", "Failed to add item to cart: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getCartItems(): Result<List<CartItem>> {
        return try {
            val userId = currentUserId ?: throw Exception("User not logged in")
            val snapshot = db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .await()
            val cartItems = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("CartRepository", "Error parsing cart item: ${e.message}")
                    null
                }
            }
            Log.d("CartRepository", "Fetched ${cartItems.size} cart items for user: $userId")
            Result.success(cartItems)
        } catch (e: Exception) {
            Log.e("CartRepository", "Failed to fetch cart items: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(itemId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: throw Exception("User not logged in")
            db.collection("users")
                .document(userId)
                .collection("cart")
                .document(itemId)
                .delete()
                .await()
            Log.d("CartRepository", "Removed item $itemId from cart for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CartRepository", "Failed to remove item from cart: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateCartItem(itemId: String, item: CartItem): Result<Unit> {
        return try {
            val userId = currentUserId ?: throw Exception("User not logged in")
            db.collection("users")
                .document(userId)
                .collection("cart")
                .document(itemId)
                .set(item, SetOptions.merge())
                .await()
            Log.d("CartRepository", "Updated item $itemId in cart for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CartRepository", "Failed to update item in cart: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .await()
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Log.d("CartRepository", "Cleared cart for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CartRepository", "Failed to clear cart: ${e.message}", e)
            Result.failure(e)
        }
    }
}