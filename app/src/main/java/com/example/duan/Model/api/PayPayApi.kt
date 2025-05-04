package com.example.duan.Model.api

import android.util.Log
import com.google.gson.Gson
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// PAYPAL REQUEST MODELS
data class PayPalRequest(
    val intent: String,
    val purchase_units: List<PayPalPurchaseUnit>,
    val application_context: PayPalApplicationContext
)

data class PayPalPurchaseUnit(
    val reference_id: String,
    val amount: PayPalAmount,
    val description: String
)

data class PayPalAmount(
    val currency_code: String,
    val value: String
)

data class PayPalApplicationContext(
    val return_url: String,
    val cancel_url: String
)

data class PayPalResponse(
    val id: String,
    val status: String,
    val links: List<PayPalLink>
)

data class PayPalLink(
    val href: String,
    val rel: String,
    val method: String
)

data class PayPalCaptureResponse(
    val id: String,
    val status: String,
    val payment_source: PaymentSource? = null,
    val purchase_units: List<PurchaseUnitResponse>? = null
)

data class PaymentSource(
    val paypal: PayPalDetail? = null
)

data class PayPalDetail(
    val email_address: String? = null,
    val account_id: String? = null,
    val name: NameDetail? = null
)

data class NameDetail(
    val given_name: String? = null,
    val surname: String? = null
)

data class PurchaseUnitResponse(
    val reference_id: String? = null,
    val payments: PaymentsDetail? = null
)

data class PaymentsDetail(
    val captures: List<CaptureDetail>? = null
)

data class CaptureDetail(
    val id: String? = null,
    val status: String? = null,
    val amount: PayPalAmount? = null,
    val final_capture: Boolean? = null,
    val create_time: String? = null,
    val update_time: String? = null
)

data class PayPalTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)


suspend fun createPayPalPayment(userId: String, totalCost: Double): String? = withContext(Dispatchers.IO) {
    val TAG = "PayPalApi"
    val clientId = "ARZnixzs4_erjJi5ZzFjErtNE3C3c-BDMau2PI2sBYtSqhETFd07DerQr6qiTvWkqY-DHHm3jhD7Ecdz"
    val clientSecret = "EE6q5953fnC9uQP4YHmeNFNev7hMwSZeIbYDfTgSRBHw1dJimR1XGzlh_iHTu8sqTfsm40vjVYRktsOV"
    val merchantPaymentId = "PAYMENT_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    val amount = BigDecimal(totalCost).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
    val orderDescription = "Thanh toán đơn hàng cho user $userId"
    val returnUrl = "com.example.duan://paypal/return"
    val cancelUrl = "com.example.duan://paypal/cancel"

    Log.d(TAG, "Starting PayPal payment creation - userId: $userId, totalCost: $totalCost, amountUSD: $amount")

    val requestBody = PayPalRequest(
        intent = "CAPTURE",
        purchase_units = listOf(
            PayPalPurchaseUnit(
                reference_id = merchantPaymentId,
                amount = PayPalAmount(
                    currency_code = "USD",
                    value = amount
                ),
                description = orderDescription
            )
        ),
        application_context = PayPalApplicationContext(
            return_url = returnUrl,
            cancel_url = cancelUrl
        )
    )

    var tokenConnection: HttpURLConnection? = null
    var connection: HttpURLConnection? = null

    try {
        // Bước 1: Lấy access token
        val accessToken = getPayPalAccessToken(clientId, clientSecret)
        if (accessToken == null) {
            Log.e(TAG, "Failed to get access token")
            throw Exception("Failed to get access token")
        }

        // Bước 2: Tạo PayPal order
        Log.d(TAG, "Creating order with access token")
        val url = URL("https://api.sandbox.paypal.com/v2/checkout/orders")
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.doOutput = true

        val jsonInputString = Gson().toJson(requestBody)
        Log.d(TAG, "Request Body: $jsonInputString")
        connection.outputStream.use { os ->
            val input = jsonInputString.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "Order creation successful, Response: $response")
            val payPalResponse = Gson().fromJson(response, PayPalResponse::class.java)
            val approvalUrl = payPalResponse.links.find { it.rel == "approve" }?.href
                ?: throw Exception("No approval URL returned in response")
            Log.d(TAG, "Approval URL: $approvalUrl")
            approvalUrl
        } else {
            val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
            Log.e(TAG, "Order creation failed, responseCode: $responseCode, Error: $errorResponse")
            throw Exception("Failed to create PayPal order: $errorResponse")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Exception during PayPal payment creation: ${e.toString()}", e)
        throw e
    } finally {
        tokenConnection?.disconnect()
        connection?.disconnect()
    }
}

/**
 * Lấy access token từ PayPal API
 */
suspend fun getPayPalAccessToken(clientId: String, clientSecret: String): String? = withContext(Dispatchers.IO) {
    val TAG = "PayPalApi"
    var tokenConnection: HttpURLConnection? = null

    try {
        val auth = "Basic " + Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        val tokenUrl = URL("https://api.sandbox.paypal.com/v1/oauth2/token")
        Log.d(TAG, "Requesting access token from: $tokenUrl")
        tokenConnection = tokenUrl.openConnection() as HttpURLConnection
        tokenConnection.requestMethod = "POST"
        tokenConnection.setRequestProperty("Authorization", auth)
        tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        tokenConnection.doOutput = true
        tokenConnection.outputStream.use { os ->
            os.write("grant_type=client_credentials".toByteArray(StandardCharsets.UTF_8))
        }

        if (tokenConnection.responseCode == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Access token request successful")
            val response = tokenConnection.inputStream.bufferedReader().use { it.readText() }
            val tokenResponse = Gson().fromJson(
                response,
                PayPalTokenResponse::class.java
            )
            return@withContext tokenResponse.access_token
        } else {
            val errorResponse = tokenConnection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
            Log.e(TAG, "Failed to get access token, responseCode: ${tokenConnection.responseCode}, Error: $errorResponse")
            return@withContext null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Exception during PayPal token request: ${e.toString()}", e)
        return@withContext null
    } finally {
        tokenConnection?.disconnect()
    }
}


suspend fun capturePayPalPayment(orderId: String): PayPalCaptureResponse? = withContext(Dispatchers.IO) {
    val TAG = "PayPalApi"
    val clientId = "ARZnixzs4_erjJi5ZzFjErtNE3C3c-BDMau2PI2sBYtSqhETFd07DerQr6qiTvWkqY-DHHm3jhD7Ecdz"
    val clientSecret = "EE6q5953fnC9uQP4YHmeNFNev7hMwSZeIbYDfTgSRBHw1dJimR1XGzlh_iHTu8sqTfsm40vjVYRktsOV"

    var connection: HttpURLConnection? = null

    try {
        // Lấy access token
        val accessToken = getPayPalAccessToken(clientId, clientSecret)
        if (accessToken == null) {
            Log.e(TAG, "Failed to get access token for capture")
            throw Exception("Failed to get access token for capture")
        }

        // Gọi API capture
        val url = URL("https://api.sandbox.paypal.com/v2/checkout/orders/$orderId/capture")
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.doOutput = true

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "Capture successful, Response: $response")
            return@withContext Gson().fromJson(response, PayPalCaptureResponse::class.java)
        } else {
            val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
            Log.e(TAG, "Capture failed, responseCode: $responseCode, Error: $errorResponse")
            throw Exception("Failed to capture PayPal payment: $errorResponse")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Exception during PayPal capture: ${e.toString()}", e)
        throw e
    } finally {
        connection?.disconnect()
    }
}

fun hmacSHA256(data: String, key: String): String {
    val algorithm = "HmacSHA256"
    val mac = Mac.getInstance(algorithm)
    val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), algorithm)
    mac.init(secretKeySpec)
    val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}