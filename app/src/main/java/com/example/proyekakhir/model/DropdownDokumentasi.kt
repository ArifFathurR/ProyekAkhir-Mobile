package com.example.proyekakhir.model

data class DropdownDokumentasi(
    val undangan_id: Int,
    val judul: String,
    val kegiatan_id: Int,
    val nama_kegiatan: String
)

data class DropdownDokumentasiResponse(
    val data: List<DropdownDokumentasi>
)
