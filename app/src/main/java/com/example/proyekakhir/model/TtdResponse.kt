package com.example.proyekakhir.model

data class TtdResponse(
    val success: Boolean,
    val message: String,
    val data: TtdData?
)

data class TtdData(
    val penerima_id: Int,
    val status_kehadiran: String,
    val waktu_presensi: String,
    val latitude: Double?,
    val longitude: Double?
)
