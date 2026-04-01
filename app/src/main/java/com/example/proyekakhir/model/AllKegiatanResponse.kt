package com.example.proyekakhir.model

import com.google.gson.annotations.SerializedName

data class AllKegiatanResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<AllKegiatan>
)

data class AllKegiatan(
    @SerializedName("id_undangan") val idUndangan: Int?,
    @SerializedName("id_penerima") val idPenerima: Int?,
    @SerializedName("nama_undangan_kegiatan") val namaUndanganKegiatan: String?,
    @SerializedName("nama_kegiatan") val namaKegiatan: String?,
    @SerializedName("tanggal") val tanggal: String?,
    @SerializedName("waktu") val waktu: String?,
    @SerializedName("waktu_selesai") val waktuSelesai: String?
)