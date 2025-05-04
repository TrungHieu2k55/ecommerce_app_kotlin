package com.example.duan.Model.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MoMoApiService {
    @POST("v2/gateway/api/create")
    suspend fun createPayment(@Body request: MoMoPaymentRequest): Response<MoMoPaymentResponse>
}

data class MoMoPaymentRequest(
    val partnerCode: String,
    val requestId: String,
    val amount: Long,
    val orderId: String,
    val orderInfo: String,
    val redirectUrl: String,
    val notifyUrl: String,
    val requestType: String = "captureWallet",
    val signature: String
)

data class MoMoPaymentResponse(
    val requestId: String,
    val orderId: String,
    val payUrl: String,
    val resultCode: Int,
    val message: String
)

