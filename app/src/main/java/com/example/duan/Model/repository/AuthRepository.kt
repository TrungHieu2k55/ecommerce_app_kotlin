package com.example.duan.Model.repository

import android.content.Context
import android.content.Intent
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("626101407112-bhtpfku97uk38guv49pmscb1fkk2fq6j.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    suspend fun register(email: String, password: String): Boolean {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        try {
            firebaseAuth.signOut()
            googleSignInClient.signOut().await()
            LoginManager.getInstance().logOut()
        } catch (e: Exception) {
            // Xử lý lỗi nếu cần
        }
    }

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    fun firebaseAuthWithGoogle(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Nếu người dùng đã đăng nhập, liên kết tài khoản Google với tài khoản hiện tại
            currentUser.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.message)
                    }
                }
        } else {
            // Nếu chưa có người dùng đăng nhập, đăng nhập bằng Google
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.message)
                    }
                }
        }
    }

    fun firebaseAuthWithFacebook(token: String, onResult: (Boolean, String?) -> Unit) {
        val credential = FacebookAuthProvider.getCredential(token)
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Nếu người dùng đã đăng nhập, liên kết tài khoản Facebook với tài khoản hiện tại
            currentUser.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        } else {
            // Nếu chưa có người dùng đăng nhập, đăng nhập bằng Facebook
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }
}