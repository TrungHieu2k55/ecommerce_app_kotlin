package com.example.duan.Model.api

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat
import java.util.*

fun createMoMoSignature(data: String, key: String): String {
    val algorithm = "HmacSHA256"
    val mac = Mac.getInstance(algorithm)
    val secretKey = SecretKeySpec(key.toByteArray(), algorithm)
    mac.init(secretKey)
    val hash = mac.doFinal(data.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}