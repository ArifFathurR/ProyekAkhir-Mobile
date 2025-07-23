package com.example.proyekakhir.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Foto(
    val id: Int,
    val dokumentasi_id: Int,
    val foto: String,
) : Parcelable
