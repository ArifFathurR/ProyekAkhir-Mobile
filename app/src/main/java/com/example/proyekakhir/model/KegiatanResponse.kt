package com.example.proyekakhir.model

data class KegiatanResponse(
    val kegiatan: List<Kegiatan>
)

data class Kegiatan(
    val id: Int,
    val nama_kegiatan: String,
    val sub_kegiatan: String,
    val tanggal: String,
    val file_undangan: String,
    val status_penerima: String
)
