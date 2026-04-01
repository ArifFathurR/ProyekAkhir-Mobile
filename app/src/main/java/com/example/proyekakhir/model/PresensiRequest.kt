package com.example.proyekakhir.model

import com.google.gson.annotations.SerializedName

data class PresensiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Presensi>
)

data class Presensi(
    @SerializedName("id") val id: Int?,
    @SerializedName("nama_kegiatan") val namaKegiatan: String?,
    @SerializedName("sub_kegiatan") val subKegiatan: String?,
    @SerializedName("tanggal") val tanggal: String?,
    @SerializedName("waktu_presensi") val waktuPresensi: String?,
    @SerializedName("ttd") val ttd: String?,
    @SerializedName("status_kehadiran") val statusKehadiran: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)