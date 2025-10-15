package com.example.proyekakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.PresensiBinding
import com.example.proyekakhir.model.Kegiatan
import com.example.proyekakhir.model.KegiatanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Presensi : AppCompatActivity() {
    private lateinit var binding: PresensiBinding
    private lateinit var adapter: PresensiAdapter
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil token dari SharedPreferences
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        token = shared.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Tombol kembali
        binding.btnKembali.setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        adapter = PresensiAdapter(emptyList()) { kegiatan ->
            // Klik tombol presensi
            val intent = Intent(this, IsiPresensiActivity::class.java)
            intent.putExtra("KEGIATAN_ID", kegiatan.id)
            startActivity(intent)
        }
        binding.recylerView.layoutManager = LinearLayoutManager(this)
        binding.recylerView.adapter = adapter

        // Load data kegiatan sedang berlangsung
        fetchKegiatanSedang()
    }

    private fun fetchKegiatanSedang() {
        ApiClient.instance.getKegiatanSedang("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(call: Call<KegiatanResponse>, response: Response<KegiatanResponse>) {
                    if (response.isSuccessful) {
                        val kegiatanList = response.body()?.kegiatan ?: emptyList()
                        adapter.updateData(kegiatanList)
                    } else {
                        Toast.makeText(this@Presensi, "Gagal memuat data (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                    Toast.makeText(this@Presensi, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
