package com.example.proyekakhir.model

import com.google.gson.annotations.SerializedName

// Model Undangan
data class Undangan(
    val id: Int,
    val judul: String
)

// Model FotoDokumentasi
data class FotoDokumentasi(
    val id: Int,
    @SerializedName("dokumentasi_id") val dokumentasiId: Int,
    val foto: String
)

// Model Dokumentasi
data class Dokumentasi(
    val id: Int,
    @SerializedName("kegiatan_id") val kegiatanId: Int,
    @SerializedName("undangan_id") val undanganId: Int,
    @SerializedName("notulensi") val notulensi: String?,
    @SerializedName("link_zoom") val linkZoom: String?,
    @SerializedName("link_materi") val linkMateri: String?,
    @SerializedName("foto_dokumentasi") val fotoDokumentasi: List<FotoDokumentasi>?,
    val undangan: Undangan? // relasi UndanganKegiatan
)

// Response dari API

