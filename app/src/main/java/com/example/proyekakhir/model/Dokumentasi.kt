package com.example.proyekakhir.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dokumentasi(
    val id: Int,
    val deskripsi: String,
    val undangan_id: Int,
    val kegiatan_id: Int,
    val foto_dokumentasi: List<Foto>
) : Parcelable

