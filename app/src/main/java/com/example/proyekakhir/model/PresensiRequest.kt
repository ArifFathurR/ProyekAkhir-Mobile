package com.example.proyekakhir.model

data class PresensiRequest(
    val penerima_undangan_id: Int,
    val ttd: String, // Base64 encoded signature
    val latitude: Double,
    val longitude: Double
)

data class PresensiResponse(
    val status: Boolean,
    val message: String,
    val data: Any?
)