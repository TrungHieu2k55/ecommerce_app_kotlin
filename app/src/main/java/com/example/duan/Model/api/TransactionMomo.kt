package com.example.duan.Model.api

import com.example.duan.Model.config.MoMoConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log

val retrofit = Retrofit.Builder()
    .baseUrl(MoMoConfig.MOMO_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val moMoApiService = retrofit.create(MoMoApiService::class.java)

suspend fun createMoMoPayment(userId: String, amount: Double): String {
    if (amount <= 0) {
        throw IllegalArgumentException("Amount must be greater than 0")
    }

    val requestId = System.currentTimeMillis().toString()
    val orderId = "ORDER_${System.currentTimeMillis()}"
    val amountLong = amount.toLong()
    val orderInfo = "Thanh toan don hang cho $userId"

    val rawData = "accessKey=${MoMoConfig.ACCESS_KEY}&amount=$amountLong&orderId=$orderId&orderInfo=$orderInfo&partnerCode=${MoMoConfig.PARTNER_CODE}&redirectUrl=${MoMoConfig.RETURN_URL}&requestId=$requestId&requestType=captureWallet"
    val signature = createMoMoSignature(rawData, MoMoConfig.SECRET_KEY)
    Log.d("MoMoPayment", "Raw Data: $rawData")
    Log.d("MoMoPayment", "Signature: $signature")

    val request = MoMoPaymentRequest(
        partnerCode = MoMoConfig.PARTNER_CODE,
        requestId = requestId,
        amount = amountLong,
        orderId = orderId,
        orderInfo = orderInfo,
        redirectUrl = MoMoConfig.RETURN_URL,
        notifyUrl = MoMoConfig.NOTIFY_URL,
        signature = signature
    )

    val response = try {
        moMoApiService.createPayment(request)
    } catch (e: Exception) {
        Log.e("MoMoPayment", "Network error: ${e.message}")
        throw Exception("Failed to connect to MoMo server: ${e.message}")
    }

    Log.d("MoMoPayment", "Response Code: ${response.code()}")
    Log.d("MoMoPayment", "Response Body: ${response.body()}")
    if (response.isSuccessful) {
        val responseBody = response.body()
        if (responseBody != null && responseBody.resultCode == 0) {
            return responseBody.payUrl
        } else {
            val errorMessage = responseBody?.message ?: "Unknown error"
            Log.e("MoMoPayment", "Error: resultCode=${responseBody?.resultCode}, message=$errorMessage")
            throw Exception("Failed to create MoMo payment: $errorMessage")
        }
    } else {
        val errorBody = response.errorBody()?.string() ?: "No error body"
        Log.e("MoMoPayment", "HTTP Error: ${response.code()}, Error Body: $errorBody")
        throw Exception("Failed to create MoMo payment: HTTP ${response.code()}, $errorBody")
    }
}