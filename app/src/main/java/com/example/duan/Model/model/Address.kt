package com.example.duan.Model.model

import kotlinx.serialization.Serializable

@Serializable
data class ProvinceResponse(
    val name: String,
    val code: Int,
    val districts: List<DistrictResponse>? = null
)

@Serializable
data class DistrictResponse(
    val name: String,
    val code: Int,
    val wards: List<WardResponse>? = null
)

@Serializable
data class WardResponse(
    val name: String,
    val code: Int
)