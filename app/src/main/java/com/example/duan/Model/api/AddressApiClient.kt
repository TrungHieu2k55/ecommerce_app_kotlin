package com.example.duan.Model.api

import com.example.duan.Model.model.DistrictResponse
import com.example.duan.Model.model.ProvinceResponse
import com.example.duan.Model.model.WardResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

object AddressApiClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        engine {
            requestTimeout = 10_000 // 10 giây
        }
    }

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun getProvinces(): List<ProvinceResponse> {
        return try {
            val responseText = client.get("https://provinces.open-api.vn/api/p/").bodyAsText()
            println("Raw provinces response: $responseText")
            val provinces = json.decodeFromString<List<ProvinceResponse>>(responseText)
            println("Parsed provinces: $provinces")
            provinces
        } catch (e: Exception) {
            println("Error fetching provinces: ${e.message}")
            emptyList()
        }
    }

    suspend fun getDistricts(provinceCode: Int): List<DistrictResponse> {
        return try {
            val responseText = client.get("https://provinces.open-api.vn/api/p/$provinceCode?depth=2").bodyAsText()
            val provinceResponse = json.decodeFromString<ProvinceResponse>(responseText)
            val districts = provinceResponse.districts ?: emptyList()
            districts
        } catch (e: Exception) {
            println("Error fetching districts: ${e.message}")
            emptyList()
        }
    }

    suspend fun getWards(districtCode: Int): List<WardResponse> {
        return try {
            val responseText = client.get("https://provinces.open-api.vn/api/d/$districtCode?depth=2").bodyAsText()
            val districtResponse = json.decodeFromString<DistrictResponse>(responseText)
            val wards = districtResponse.wards ?: emptyList()
            wards
        } catch (e: Exception) {
            println("Error fetching wards: ${e.message}")
            emptyList()
        }
    }
}