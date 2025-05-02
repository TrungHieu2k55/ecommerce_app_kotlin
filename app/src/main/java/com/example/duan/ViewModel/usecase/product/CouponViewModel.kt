package com.example.duan.ViewModel.usecase.product

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duan.Model.model.Coupon
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CouponViewModel @Inject constructor() : ViewModel() {
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons

    // Hàm hỗ trợ để chuyển đổi String thành Date
    private fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(dateString)
        } catch (e: Exception) {
            Log.e("CouponViewModel", "Failed to parse date: $dateString, error: ${e.message}")
            null
        }
    }

    fun applyCoupon(code: String, orderTotal: Long, onDiscountCalculated: (Double) -> Unit) {
        viewModelScope.launch {
            Firebase.firestore.collection("coupons")
                .whereEqualTo("code", code)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        _errorMessage.value = "Invalid coupon code"
                        _appliedCoupon.value = null
                        onDiscountCalculated(0.0)
                        return@addOnSuccessListener
                    }

                    val coupon = snapshot.documents.first().toObject(Coupon::class.java)
                    coupon?.let {
                        val currentDate = Date()
                        val validFromDate = parseDate(it.validFrom)
                        val validToDate = parseDate(it.validTo)

                        if (validFromDate != null && currentDate.before(validFromDate)) {
                            _errorMessage.value = "Coupon not yet valid"
                            _appliedCoupon.value = null
                            onDiscountCalculated(0.0)
                            return@addOnSuccessListener
                        }
                        if (validToDate != null && currentDate.after(validToDate)) {
                            _errorMessage.value = "Coupon has expired"
                            _appliedCoupon.value = null
                            onDiscountCalculated(0.0)
                            return@addOnSuccessListener
                        }
                        if (orderTotal < it.minOrderValue) {
                            _errorMessage.value = "Minimum order value not met"
                            _appliedCoupon.value = null
                            onDiscountCalculated(0.0)
                            return@addOnSuccessListener
                        }

                        val discount = if (it.type == "percentage") {
                            val discountAmount = (orderTotal * it.discount / 100)
                            if (discountAmount > it.maxDiscount) it.maxDiscount else discountAmount
                        } else {
                            it.discount
                        }

                        _appliedCoupon.value = it
                        _errorMessage.value = null
                        onDiscountCalculated(discount)
                    }
                }
                .addOnFailureListener { e ->
                    _errorMessage.value = "Error applying coupon: ${e.message}"
                    _appliedCoupon.value = null
                    onDiscountCalculated(0.0)
                }
        }
    }

    fun fetchCoupons() {
        viewModelScope.launch {
            Firebase.firestore.collection("coupons")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener { snapshot ->
                    val couponList = snapshot.documents.mapNotNull { it.toObject(Coupon::class.java) }
                    val currentDate = Date()
                    val validCoupons = couponList.filter { coupon ->
                        val validFromDate = parseDate(coupon.validFrom)
                        val validToDate = parseDate(coupon.validTo)
                        val fromValid = validFromDate == null || !currentDate.before(validFromDate)
                        val toValid = validToDate == null || !currentDate.after(validToDate)
                        fromValid && toValid
                    }
                    _coupons.value = validCoupons
                }
                .addOnFailureListener { e ->
                    _errorMessage.value = "Error fetching coupons: ${e.message}"
                }
        }
    }

    fun clearCoupon() {
        _appliedCoupon.value = null
        _errorMessage.value = null
    }
}