package com.example.duan.ViewModel.usecase.auth

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.model.Address
import com.example.duan.Model.model.Order
import com.example.duan.Model.model.UserProfile
import com.example.duan.Model.repository.AuthRepository
import com.example.duan.Model.repository.FirestoreRepository
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _googleLoginState = MutableStateFlow<GoogleLoginState>(GoogleLoginState.Idle)
    val googleLoginState: StateFlow<GoogleLoginState> = _googleLoginState

    private val _facebookLoginState = MutableStateFlow<FacebookLoginState>(FacebookLoginState.Idle)
    val facebookLoginState: StateFlow<FacebookLoginState> = _facebookLoginState

    private val _addressState = MutableStateFlow<AddressState>(AddressState.Idle)
    val addressState: StateFlow<AddressState> = _addressState

    private val auth = FirebaseAuth.getInstance()
    val isLoggedIn = mutableStateOf(auth.currentUser != null)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory

    val errorMessage = mutableStateOf<String?>(null)

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        loadUserProfile()
    }

    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getDisplayName(): String? {
        return auth.currentUser?.displayName
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val success = authRepository.login(email, password)
            if (success) {
                isLoggedIn.value = true
                loadUserProfile()
                _loginState.value = LoginState.Success
            } else {
                isLoggedIn.value = false
                _loginState.value = LoginState.Error("Đăng nhập thất bại")
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val success = authRepository.register(email, password)
            val uid = auth.currentUser?.uid
            if (success && uid != null) {
                val userProfile = UserProfile(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    phoneNumber = "",
                    addresses = emptyList(),
                    selectedAddress = null,
                    paymentMethods = emptyList(),
                    photoUrl = null
                )
                try {
                    firestore.collection("users").document(uid)
                        .set(userProfile, SetOptions.merge())
                        .await()
                    Log.d("AuthViewModel", "User profile created/updated for $uid with name $displayName")
                    _registerState.value = RegisterState.Success
                    isLoggedIn.value = true
                    loadUserProfile()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to create user profile", e)
                    _registerState.value = RegisterState.Error("Không thể tạo hồ sơ người dùng: ${e.message}")
                }
            } else {
                _registerState.value = RegisterState.Error("Đăng ký thất bại")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            auth.signOut()
            // Xóa cache Firestore
            try {
                firestore.clearPersistence().await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to clear Firestore cache: ${e.message}", e)
            }
            isLoggedIn.value = false
            _userProfile.value = null
            _orderHistory.value = emptyList()
            resetLoginState()
            resetGoogleLoginState()
            resetFacebookLoginState()
            resetAddressState()
        }
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _googleLoginState.value = GoogleLoginState.Loading
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    authRepository.firebaseAuthWithGoogle(idToken) { success, error ->
                        if (success) {
                            isLoggedIn.value = true
                            handleUserProfileAfterLogin()
                            _googleLoginState.value = GoogleLoginState.Success
                        } else {
                            Log.e("AuthViewModel", "Google auth failed: $error")
                            _googleLoginState.value = GoogleLoginState.Error(error)
                        }
                    }
                } else {
                    _googleLoginState.value = GoogleLoginState.Error("ID Token null")
                }
            } catch (e: ApiException) {
                Log.e("AuthViewModel", "Google Sign-In failed", e)
                _googleLoginState.value = GoogleLoginState.Error("Google Sign-In failed: ${e.message}")
            }
        }
    }

    fun handleFacebookLoginResult(loginResult: LoginResult) {
        viewModelScope.launch {
            _facebookLoginState.value = FacebookLoginState.Loading
            val token = loginResult.accessToken.token
            authRepository.firebaseAuthWithFacebook(token) { success, error ->
                if (success) {
                    isLoggedIn.value = true
                    handleUserProfileAfterLogin()
                    _facebookLoginState.value = FacebookLoginState.Success
                } else {
                    Log.e("AuthViewModel", "Facebook auth failed: $error")
                    _facebookLoginState.value = FacebookLoginState.Error(error)
                }
            }
        }
    }

    private fun handleUserProfileAfterLogin() {
        val uid = auth.currentUser?.uid
        val email = auth.currentUser?.email
        val name = auth.currentUser?.displayName
        val photoUrl = auth.currentUser?.photoUrl?.toString()

        if (uid != null && email != null) {
            viewModelScope.launch {
                try {
                    val existingProfileResult = firestoreRepository.getUserProfile(uid)
                    val existingProfile = existingProfileResult.getOrNull()

                    if (existingProfile == null) {
                        val userProfile = UserProfile(
                            uid = uid,
                            displayName = name ?: "",
                            email = email,
                            phoneNumber = "",
                            addresses = emptyList(),
                            selectedAddress = null,
                            paymentMethods = emptyList(),
                            photoUrl = photoUrl
                        )
                        firestore.collection("users").document(uid)
                            .set(userProfile, SetOptions.merge())
                            .await()
                        Log.d("AuthViewModel", "Created new profile for user: $name")
                    } else {
                        Log.d("AuthViewModel", "User already has profile: ${existingProfile.displayName}")
                    }

                    loadUserProfile()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error handling user profile", e)
                    errorMessage.value = "Error handling profile: ${e.message}"
                }
            }
        }
    }

    private fun loadUserProfile() {
        val uid = getCurrentUserId()
        Log.d("AuthViewModel", "Loading user profile for UID: $uid")

        if (uid != null) {
            firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("AuthViewModel", "Error listening to user profile: ${e.message}", e)
                        errorMessage.value = e.message ?: "Lỗi tải hồ sơ"
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        _userProfile.value = profile
                        Log.d("AuthViewModel", "Profile loaded: ${profile?.uid}")
                    } else {
                        Log.w("AuthViewModel", "Profile does not exist for UID: $uid")
                        _userProfile.value = null
                    }
                }
        } else {
            Log.w("AuthViewModel", "Cannot load profile - UID is null")
            _userProfile.value = null
        }
    }

    fun updateUserProfile(displayName: String, phoneNumber: String): Task<Void> {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e("AuthViewModel", "Cannot update profile - User ID is null")
            return com.google.android.gms.tasks.Tasks.forException(Exception("User ID is null"))
        }
        val updates = mapOf(
            "displayName" to displayName,
            "phoneNumber" to phoneNumber
        )
        return firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "User profile updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Failed to update user profile: ${e.message}", e)
                errorMessage.value = "Failed to update profile: ${e.message}"
            }
    }

    fun updatePaymentMethods(methods: List<String>) {
        val uid = getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                val result = firestoreRepository.updatePaymentMethods(uid, methods)
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "Payment methods updated for $uid")
                } else {
                    Log.e("AuthViewModel", "Failed to update payment methods", result.exceptionOrNull())
                    errorMessage.value = result.exceptionOrNull()?.message ?: "Cập nhật phương thức thanh toán thất bại"
                }
            }
        }
    }

    fun updateProfilePicture(photoUrl: String): Task<Void> {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e("AuthViewModel", "Cannot update profile picture - User ID is null")
            return com.google.android.gms.tasks.Tasks.forException(Exception("User ID is null"))
        }
        return firestore.collection("users").document(userId)
            .update("photoUrl", photoUrl)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "Photo URL updated successfully: $photoUrl")
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Failed to update photo URL: ${e.message}", e)
                errorMessage.value = "Failed to update photo URL: ${e.message}"
            }
    }

    fun addAddress(newAddress: Address) {
        val userId = getCurrentUserId() ?: run {
            _addressState.value = AddressState.Error("User ID is null")
            return
        }
        val currentAddresses = _userProfile.value?.addresses ?: emptyList()

        // Kiểm tra trùng lặp tiêu đề
        if (currentAddresses.any { it.title == newAddress.title }) {
            _addressState.value = AddressState.Error("Address with title '${newAddress.title}' already exists")
            return
        }

        val updatedAddresses = currentAddresses.toMutableList().apply { add(newAddress) }

        viewModelScope.launch {
            _addressState.value = AddressState.Loading
            try {
                firestore.collection("users").document(userId)
                    .update("addresses", updatedAddresses)
                    .await()
                _userProfile.value = _userProfile.value?.copy(addresses = updatedAddresses)
                _addressState.value = AddressState.Success("Address added successfully")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to add address: ${e.message}", e)
                _addressState.value = AddressState.Error("Failed to add address: ${e.message}")
            }
        }
    }

    fun editAddress(oldTitle: String, updatedAddress: Address) {
        val userId = getCurrentUserId() ?: run {
            _addressState.value = AddressState.Error("User ID is null")
            return
        }
        val currentAddresses = _userProfile.value?.addresses ?: emptyList()

        // Kiểm tra xem địa chỉ có tồn tại không
        val addressExists = currentAddresses.any { it.title == oldTitle }
        if (!addressExists) {
            _addressState.value = AddressState.Error("Address with title '$oldTitle' does not exist")
            return
        }

        // Kiểm tra trùng lặp tiêu đề (trừ chính nó)
        if (updatedAddress.title != oldTitle && currentAddresses.any { it.title == updatedAddress.title }) {
            _addressState.value = AddressState.Error("Address with title '${updatedAddress.title}' already exists")
            return
        }

        // Cập nhật địa chỉ
        val updatedAddresses = currentAddresses.map { if (it.title == oldTitle) updatedAddress else it }

        viewModelScope.launch {
            _addressState.value = AddressState.Loading
            try {
                // Nếu tiêu đề thay đổi và địa chỉ này đang được chọn, cập nhật selectedAddress
                if (oldTitle == _userProfile.value?.selectedAddress && oldTitle != updatedAddress.title) {
                    firestore.collection("users").document(userId)
                        .update(
                            mapOf(
                                "addresses" to updatedAddresses,
                                "selectedAddress" to updatedAddress.title
                            )
                        )
                        .await()
                    _userProfile.value = _userProfile.value?.copy(
                        addresses = updatedAddresses,
                        selectedAddress = updatedAddress.title
                    )
                } else {
                    firestore.collection("users").document(userId)
                        .update("addresses", updatedAddresses)
                        .await()
                    _userProfile.value = _userProfile.value?.copy(addresses = updatedAddresses)
                }
                _addressState.value = AddressState.Success("Address updated successfully")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to edit address: ${e.message}", e)
                _addressState.value = AddressState.Error("Failed to edit address: ${e.message}")
            }
        }
    }

    fun deleteAddress(title: String) {
        val userId = getCurrentUserId() ?: run {
            _addressState.value = AddressState.Error("User ID is null")
            return
        }
        val currentAddresses = _userProfile.value?.addresses ?: emptyList()

        // Kiểm tra xem địa chỉ có tồn tại không
        val addressExists = currentAddresses.any { it.title == title }
        if (!addressExists) {
            _addressState.value = AddressState.Error("Address with title '$title' does not exist")
            return
        }

        val updatedAddresses = currentAddresses.filter { it.title != title }

        viewModelScope.launch {
            _addressState.value = AddressState.Loading
            try {
                // Nếu địa chỉ bị xóa là địa chỉ đang được chọn, đặt selectedAddress về null hoặc địa chỉ đầu tiên
                if (title == _userProfile.value?.selectedAddress) {
                    val newSelectedAddress = updatedAddresses.firstOrNull()?.title
                    firestore.collection("users").document(userId)
                        .update(
                            mapOf(
                                "addresses" to updatedAddresses,
                                "selectedAddress" to newSelectedAddress
                            )
                        )
                        .await()
                    _userProfile.value = _userProfile.value?.copy(
                        addresses = updatedAddresses,
                        selectedAddress = newSelectedAddress
                    )
                } else {
                    firestore.collection("users").document(userId)
                        .update("addresses", updatedAddresses)
                        .await()
                    _userProfile.value = _userProfile.value?.copy(addresses = updatedAddresses)
                }
                _addressState.value = AddressState.Success("Address deleted successfully")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to delete address: ${e.message}", e)
                _addressState.value = AddressState.Error("Failed to delete address: ${e.message}")
            }
        }
    }

    fun updateSelectedAddress(title: String) {
        val userId = getCurrentUserId() ?: run {
            _addressState.value = AddressState.Error("User ID is null")
            return
        }
        val currentAddresses = _userProfile.value?.addresses ?: emptyList()

        // Kiểm tra xem địa chỉ có tồn tại không
        val addressExists = currentAddresses.any { it.title == title }
        if (!addressExists) {
            _addressState.value = AddressState.Error("Address with title '$title' does not exist")
            return
        }

        viewModelScope.launch {
            _addressState.value = AddressState.Loading
            try {
                firestore.collection("users").document(userId)
                    .update("selectedAddress", title)
                    .await()
                _userProfile.value = _userProfile.value?.copy(selectedAddress = title)
                _addressState.value = AddressState.Success("Selected address updated successfully")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update selected address: ${e.message}", e)
                _addressState.value = AddressState.Error("Failed to update selected address: ${e.message}")
            }
        }
    }

    fun loadOrderHistory() {
        val uid = getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                val result = firestoreRepository.getOrderHistory(uid)
                if (result.isSuccess) {
                    _orderHistory.value = result.getOrNull() ?: emptyList()
                    Log.d("AuthViewModel", "Order history loaded: ${_orderHistory.value.size} orders")
                } else {
                    Log.e("AuthViewModel", "Failed to load order history", result.exceptionOrNull())
                    errorMessage.value = result.exceptionOrNull()?.message ?: "Lỗi tải lịch sử đơn hàng"
                }
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    fun resetGoogleLoginState() {
        _googleLoginState.value = GoogleLoginState.Idle
    }

    fun resetFacebookLoginState() {
        _facebookLoginState.value = FacebookLoginState.Idle
    }

    fun resetAddressState() {
        _addressState.value = AddressState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String?) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String?) : RegisterState()
}

sealed class GoogleLoginState {
    object Idle : GoogleLoginState()
    object Loading : GoogleLoginState()
    object Success : GoogleLoginState()
    data class Error(val message: String?) : GoogleLoginState()
}

sealed class FacebookLoginState {
    object Idle : FacebookLoginState()
    object Loading : FacebookLoginState()
    object Success : FacebookLoginState()
    data class Error(val message: String?) : FacebookLoginState()
}

sealed class AddressState {
    object Idle : AddressState()
    object Loading : AddressState()
    data class Success(val message: String) : AddressState()
    data class Error(val message: String) : AddressState()
}