package com.example.proyekakhir.model

import com.google.gson.annotations.SerializedName

data class DokumentasiResponse(
    val status: Boolean,
    val message: String?,
    @SerializedName("data") val dokumentasi: List<Dokumentasi>?
)