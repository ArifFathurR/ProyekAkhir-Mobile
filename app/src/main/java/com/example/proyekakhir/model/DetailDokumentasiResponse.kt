package com.example.proyekakhir.model

import com.google.gson.annotations.SerializedName

data class DetailDokumentasiResponse(
    val notulensi: String,
    @SerializedName("link_zoom")
    val linkZoom: String,
    @SerializedName("link_materi")
    val linkMateri: String,
    val foto: List<FotoDetail>
)

data class FotoDetail(
    val id: Int,
    @SerializedName("file_foto")
    val fileFoto: String,
    @SerializedName("created_at")
    val createdAt: String
)