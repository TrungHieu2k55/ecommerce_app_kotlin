package com.example.duan.View.home

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import com.example.duan.Model.config.CloudinaryConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePictureScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Lắng nghe userProfile từ ViewModel (StateFlow)
    val userProfile by authViewModel.userProfile.collectAsState()

    // State để lưu photoUrl, khởi tạo từ userProfile, đảm bảo HTTPS
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUrl by remember {
        mutableStateOf(userProfile?.photoUrl?.replace("http://", "https://") ?: "")
    }
    var publicId by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf(userProfile?.displayName ?: "") }
    var phoneNumber by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Biến để theo dõi xem đã upload ảnh mới chưa
    var hasUploadedNewPhoto by remember { mutableStateOf(false) }

    // Cập nhật state từ userProfile chỉ khi chưa upload ảnh mới
    LaunchedEffect(userProfile) {
        userProfile?.let {
            if (!hasUploadedNewPhoto) {
                photoUrl = it.photoUrl?.replace("http://", "https://") ?: ""
            }
            displayName = it.displayName ?: ""
            phoneNumber = it.phoneNumber ?: ""
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            isUploading = true
            uploadError = null

            // Kiểm tra trạng thái đăng nhập trước khi upload
            if (!authViewModel.isLoggedIn.value) {
                uploadError = "Please log in to upload a profile picture."
                Log.e("EditProfilePicture", "User is not logged in")
                isUploading = false
                return@let
            }

            coroutineScope.launch {
                try {
                    CloudinaryConfig.getCloudinary().url().generate(
                        MediaManager.get().upload(it)
                            .unsigned("ecommerce")
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String) {
                                    Log.d("Cloudinary", "Upload started for request: $requestId")
                                }

                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                                    Log.d("Cloudinary", "Upload progress: $bytes/$totalBytes")
                                }

                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                    val url = resultData["url"] as? String
                                    val pid = resultData["public_id"] as? String
                                    if (url != null) {
                                        // Chuyển URL sang HTTPS
                                        val secureUrl = url.replace("http://", "https://")
                                        photoUrl = secureUrl
                                        publicId = pid
                                        hasUploadedNewPhoto = true // Đánh dấu đã upload ảnh mới
                                        isUploading = false
                                        Log.d("Cloudinary", "Upload success, URL: $secureUrl, Public ID: $pid")
                                    } else {
                                        uploadError = "Failed to get URL from Cloudinary"
                                        isUploading = false
                                        Log.e("Cloudinary", "Upload success but no URL in response")
                                    }
                                }

                                override fun onError(requestId: String, error: ErrorInfo) {
                                    uploadError = error.description
                                    isUploading = false
                                    Log.e("Cloudinary", "Upload failed: ${error.description}")
                                }

                                override fun onReschedule(requestId: String, error: ErrorInfo) {
                                    Log.d("Cloudinary", "Upload rescheduled: ${error.description}")
                                }
                            })
                            .dispatch()
                    )
                } catch (e: Exception) {
                    uploadError = e.message
                    isUploading = false
                    Log.e("Cloudinary", "Upload exception: ${e.message}", e)
                }
            }
        }
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            uploadError = "Permission to access storage denied"
            Log.e("EditProfilePicture", "Storage permission denied")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Profile Picture") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUploading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Uploading image...", color = Color.Gray)
            } else if (uploadError != null) {
                Text(
                    text = "Error: $uploadError",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Thêm log để kiểm tra recomposition
            Log.d("EditProfilePicture", "Recomposition - photoUrl: $photoUrl")

            if (photoUrl.isNotEmpty()) {
                // Sử dụng LaunchedEffect để đảm bảo painter được tạo lại khi photoUrl thay đổi
                val painter = rememberAsyncImagePainter(
                    model = photoUrl,
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Loading -> {
                                Log.d("EditProfilePicture", "Loading image from URL: $photoUrl")
                            }
                            is AsyncImagePainter.State.Success -> {
                                Log.d("EditProfilePicture", "Image loaded successfully: $photoUrl")
                            }
                            is AsyncImagePainter.State.Error -> {
                                Log.e("EditProfilePicture", "Failed to load image: ${state.result.throwable.message}")
                                uploadError = "Failed to load image: ${state.result.throwable.message}"
                            }
                            else -> {}
                        }
                    }
                )

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            publicId?.let {
                Text(
                    text = "Public ID: $it",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageLauncher.launch("image/*")
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
            ) {
                Text("Choose Picture", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("EditProfilePicture", "Save button clicked")
                    // Kiểm tra trạng thái đăng nhập trước khi lưu
                    if (!authViewModel.isLoggedIn.value) {
                        uploadError = "Please log in to update your profile picture."
                        Log.e("EditProfilePicture", "User is not logged in")
                        return@Button
                    }

                    if (photoUrl.isNotEmpty()) {
                        Log.d("EditProfilePicture", "Saving photoUrl to Firestore: $photoUrl")
                        authViewModel.updateProfilePicture(photoUrl)
                            .addOnSuccessListener {
                                Log.d("EditProfilePicture", "Photo URL updated successfully in Firestore")
                                uploadError = "Profile picture updated successfully!"
                                hasUploadedNewPhoto = false // Reset sau khi lưu thành công
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditProfilePicture", "Failed to update photo URL in Firestore: ${e.message}", e)
                                uploadError = "Failed to update profile picture: ${e.message}"
                            }
                    } else {
                        Log.w("EditProfilePicture", "Photo URL is empty, skipping Firestore update")
                        uploadError = "Please upload a photo first."
                    }

                    authViewModel.updateUserProfile(displayName, phoneNumber)
                        .addOnSuccessListener {
                            Log.d("EditProfilePicture", "User profile updated successfully")
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditProfilePicture", "Failed to update user profile: ${e.message}", e)
                            uploadError = "Failed to update profile: ${e.message}"
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
            ) {
                Text("Save", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}