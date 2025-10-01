package com.example.proyekakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.DokumentasiKegiatanSayaBinding
import com.example.proyekakhir.model.DokumentasiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DokumentasiSayaActivity : AppCompatActivity() {

    private lateinit var binding: DokumentasiKegiatanSayaBinding
    private lateinit var adapter: DokumentasiSayaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DokumentasiKegiatanSayaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol kembali
        binding.btnKembali.setOnClickListener { finish() }

        // Tombol tambah
        binding.btnTambah.setOnClickListener {
            startActivity(Intent(this, TambahDokumentasiActivity::class.java))
        }

        setupRecyclerView()
        setupSwipeRefresh()
        fetchDokumentasi()
    }

    private fun setupRecyclerView() {
        binding.recylerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSwipeRefresh() {
        // Pastikan layout XML menggunakan <androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
        binding.swipeRefresh.setOnRefreshListener {
            fetchDokumentasi()
        }
    }

    private fun fetchDokumentasi() {
        binding.swipeRefresh.isRefreshing = true

        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val token = shared.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login kembali", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.instance.getDokumentasiSaya("Bearer $token")
            .enqueue(object : Callback<DokumentasiResponse> {
                override fun onResponse(
                    call: Call<DokumentasiResponse>,
                    response: Response<DokumentasiResponse>
                ) {
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        // Urutkan data berdasarkan ID descending (terbaru di atas)
                        val list = response.body()?.dokumentasi
                            ?.sortedByDescending { it.id } ?: emptyList()

                        adapter = DokumentasiSayaAdapter(
                            list.toMutableList(),
                            onEditClick = { dokumentasi ->
                                Toast.makeText(
                                    this@DokumentasiSayaActivity,
                                    "Edit ${dokumentasi.id}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onDeleteClick = { dokumentasi ->
                                Toast.makeText(
                                    this@DokumentasiSayaActivity,
                                    "Delete ${dokumentasi.id}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        binding.recylerView.adapter = adapter
                    } else {
                        Toast.makeText(
                            this@DokumentasiSayaActivity,
                            "Gagal memuat data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DokumentasiResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(
                        this@DokumentasiSayaActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
