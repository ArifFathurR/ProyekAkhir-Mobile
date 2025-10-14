package com.example.proyekakhir

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var adapter: KegiatanAdapter
    private var currentTab = "akan_datang"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil token dari SharedPreferences
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val userName = shared.getString("USER_NAME", "Pengguna")
        token = shared.getString("TOKEN", null)

        binding.txUsername.text = "$userName"

        // Cek token
        if (token.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Setup adapter
        setupAdapter()

        // Set default tab (Akan Datang aktif)
        setActiveTab(true)
        fetchKegiatan()

        //Lihat Dokumentasi
        binding.btnLihatDokumentasi.setOnClickListener {
            val intent = Intent(this, LihatDokumentasi::class.java)
            startActivity(intent)
        }

        // Tab click listeners
        binding.tabAkanDatang.setOnClickListener {
            currentTab = "akan_datang"
            setActiveTab(true)
            fetchKegiatan()
        }

        binding.tabSelesai.setOnClickListener {
            currentTab = "selesai"
            setActiveTab(false)
            fetchKegiatanSelesai()
        }

        // Button listeners
        binding.btnBuatDokumentasi.setOnClickListener {
            val intent = Intent(this, DokumentasiSayaActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupAdapter() {
        adapter = KegiatanAdapter(
            emptyList(),
            onUndanganClick = { fileUrl ->
                openPdf(fileUrl)
            },
            onDetailClick = { penerimaId ->
                val intent = Intent(this, DetailDokumentasiActivity::class.java)
                intent.putExtra("PENERIMA_ID", penerimaId)
                startActivity(intent)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setActiveTab(isAkanDatangActive: Boolean) {
        if (isAkanDatangActive) {
            binding.tabAkanDatang.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.tab_active_bg)
            )
            binding.tvAkanDatang.setTextColor(
                ContextCompat.getColor(this, R.color.tab_active_text)
            )

            binding.tabSelesai.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.tab_inactive_bg)
            )
            binding.tvSelesai.setTextColor(
                ContextCompat.getColor(this, R.color.tab_inactive_text)
            )
        } else {
            binding.tabSelesai.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.tab_active_bg)
            )
            binding.tvSelesai.setTextColor(
                ContextCompat.getColor(this, R.color.tab_active_text)
            )

            binding.tabAkanDatang.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.tab_inactive_bg)
            )
            binding.tvAkanDatang.setTextColor(
                ContextCompat.getColor(this, R.color.tab_inactive_text)
            )
        }
    }

    private fun fetchKegiatan() {
        ApiClient.instance.getKegiatan("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(
                    call: Call<KegiatanResponse>,
                    response: Response<KegiatanResponse>
                ) {
                    if (response.isSuccessful) {
                        val kegiatanList = response.body()?.kegiatan ?: emptyList()
                        adapter.updateData(kegiatanList, false)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Gagal memuat data: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun fetchKegiatanSelesai() {
        ApiClient.instance.getKegiatanSelesai("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(
                    call: Call<KegiatanResponse>,
                    response: Response<KegiatanResponse>
                ) {
                    if (response.isSuccessful) {
                        val kegiatanList = response.body()?.kegiatan ?: emptyList()
                        adapter.updateData(kegiatanList, true)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Gagal memuat data selesai: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun logout() {
        val shared = getSharedPreferences("APP", MODE_PRIVATE)

        ApiClient.instance.logout("Bearer $token")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    shared.edit().remove("TOKEN").apply()

                    Toast.makeText(
                        this@MainActivity,
                        "Logout berhasil",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal logout: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
            Toast.makeText(
                this,
                "Tidak ada aplikasi PDF terpasang",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}