package com.example.proyekakhir
import com.example.proyekakhir.auth.LoginRequest
import com.example.proyekakhir.auth.LoginResponse
import retrofit2.Call
import retrofit2.http.*
import com.example.proyekakhir.model.KegiatanResponse

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

//    @GET("profile")
//    fun getProfile(@Header("Authorization") token: String): Call<User>

    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<Void>

    @GET("pegawai/kegiatan")
    fun getKegiatan(@Header("Authorization") token: String): Call<KegiatanResponse>
}
