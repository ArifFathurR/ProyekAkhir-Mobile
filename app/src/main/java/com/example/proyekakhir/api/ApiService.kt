package com.example.proyekakhir.api
import com.example.proyekakhir.auth.LoginRequest
import com.example.proyekakhir.auth.LoginResponse
import com.example.proyekakhir.model.DetailDokumentasiResponse
import retrofit2.Call
import retrofit2.http.*
import com.example.proyekakhir.model.Dokumentasi
import com.example.proyekakhir.model.KegiatanResponse
import com.example.proyekakhir.model.DokumentasiResponse
import com.example.proyekakhir.model.DokumentasiSelesaiResponse
import com.example.proyekakhir.model.DropdownDokumentasiResponse
import com.example.proyekakhir.model.TtdResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

//    @GET("profile")
//    fun getProfile(@Header("Authorization") token: String): Call<User>

    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<Void>

    @GET("pegawai/kegiatan")
    fun getKegiatan(@Header("Authorization") token: String): Call<KegiatanResponse>

    @GET("pegawai/kegiatan/sedang")
    fun getKegiatanSedang(@Header("Authorization") token: String): Call<KegiatanResponse>

    @GET("dokumentasi")
    fun getDokumentasiSaya(@Header("Authorization") token: String): Call<DokumentasiResponse>
    @GET("pegawai/dropdown-dokumentasi")

    fun getDropdownDokumentasi(@Header("Authorization") token: String): Call<DropdownDokumentasiResponse>

    @Multipart
    @POST("dokumentasi_store")
    fun createDokumentasi(
        @Part("undangan_id") undanganId: RequestBody,
        @Part("kegiatan_id") kegiatanId: RequestBody,
        @Part("notulensi") deskripsi: RequestBody,
        @Part("link_zoom") link_zoom: RequestBody,
        @Part("link_materi") link_materi: RequestBody,
        @Part foto: List<MultipartBody.Part>?,
        @Header("Authorization") token: String
    ): Call<Dokumentasi>

    @Multipart
    @POST("dokumentasi/{id}")
    fun updateDokumentasi(
        @Path("id") id: Int,
        @Part("undangan_id") undanganId: RequestBody,
        @Part("kegiatan_id") kegiatanId: RequestBody,
        @Part("notulensi") deskripsi: RequestBody,
        @Part("link_zoom") link_zoom: RequestBody,
        @Part("link_materi") link_materi: RequestBody,
        @Part foto: List<MultipartBody.Part>?,
        @Part("deleted_foto_ids") deleted_foto_ids: RequestBody,
        @Header("Authorization") token: String
    ): Call<Dokumentasi>

    @DELETE("dokumentasi/{id}")
    fun deleteDokumentasi(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>

    // Endpoint kegiatan selesai
    @GET("pegawai/kegiatan/selesai")
    fun getKegiatanSelesai(@Header("Authorization") token: String): Call<KegiatanResponse>

    // Endpoint detail dokumentasi berdasarkan penerima_id
    @GET("pegawai/dokumentasi/{penerima_id}")
    fun getDetailDokumentasi(
        @Header("Authorization") token: String,
        @Path("penerima_id") penerimaId: Int
    ): Call<DetailDokumentasiResponse>

    @GET("dokumentasi/selesai")
    fun getDokumentasiSelesai(
        @Header("Authorization") token: String
    ): Call<DokumentasiSelesaiResponse>

    // 🔹 Kirim tanda tangan (TTD)
    @FormUrlEncoded
    @POST("pegawai/ttd")
    fun storeTtd(
        @Header("Authorization") token: String,
        @Field("penerima_id") penerimaId: Int,
        @Field("ttd") ttd: String,
        @Field("latitude") latitude: Double?,
        @Field("longitude") longitude: Double?
    ): Call<TtdResponse>

}