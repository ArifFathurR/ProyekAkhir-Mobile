package com.example.proyekakhir.model

import com.google.gson.annotations.SerializedName

data class DokumentasiSelesai(
    val id: Int,
    @SerializedName("nama_kegiatan") val namaKegiatan: String,
    @SerializedName("sub_kegiatan") val subKegiatan: String,
    val tanggal: String,
    @SerializedName("status_pelaksanaan") val statusPelaksanaan: String,
    val notulensi: String?,
    @SerializedName("link_zoom") val linkZoom: String?,
    @SerializedName("link_materi") val linkMateri: String?,
    @SerializedName("foto_dokumentasi") val fotoDokumentasi: List<String> // hanya list URL
)

data class DokumentasiSelesaiResponse(
    val dokumentasi: List<DokumentasiSelesai>
)
