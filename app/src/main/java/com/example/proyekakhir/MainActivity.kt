package com.example.proyekakhir

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.ActivityMainBinding
import com.example.proyekakhir.model.KegiatanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil token dari SharedPreferences
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val userName = shared.getString("USER_NAME", "Pengguna")
        token = shared.getString("TOKEN", null)

        binding.txUsername.text = "$userName"

        if (token.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        fetchKegiatan()

        binding.btnBuatDokumentasi.setOnClickListener {
            val intent = Intent(this, DokumentasiSayaActivity::class.java)
            startActivity(intent)
        }

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            ApiClient.instance.logout("Bearer $token").enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    shared.edit().remove("TOKEN").apply()
                    Toast.makeText(this@MainActivity, "Logout berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Gagal logout: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchKegiatan() {
        ApiClient.instance.getKegiatan("Bearer $token").enqueue(object : Callback<KegiatanResponse> {
            override fun onResponse(call: Call<KegiatanResponse>, response: Response<KegiatanResponse>) {
                if (response.isSuccessful) {
                    val kegiatanList = response.body()?.kegiatan ?: emptyList()
                    val adapter = KegiatanAdapter(kegiatanList) { fileUrl ->
                        openPdf(fileUrl)
                    }
                    binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    binding.recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@MainActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openPdf(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi PDF terpasang", Toast.LENGTH_SHORT).show()
        }
    }
}
