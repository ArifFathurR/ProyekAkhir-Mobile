package com.example.proyekakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.DokumentasiKegiatanBinding
import com.example.proyekakhir.model.DokumentasiSelesaiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LihatDokumentasi : AppCompatActivity() {
    private lateinit var binding: DokumentasiKegiatanBinding
    private lateinit var adapter: LihatDokumentasiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DokumentasiKegiatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener { finish() }

        setupRecyclerView()
        fetchDokumentasiSelesai()
    }

    private fun setupRecyclerView() {
        adapter = LihatDokumentasiAdapter()

        binding.recylerView.layoutManager = LinearLayoutManager(this)
        binding.recylerView.adapter = adapter
    }

    private fun fetchDokumentasiSelesai() {
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val token = shared.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login kembali", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.instance.getDokumentasiSelesai("Bearer $token")
            .enqueue(object : Callback<DokumentasiSelesaiResponse> {
                override fun onResponse(
                    call: Call<DokumentasiSelesaiResponse>,
                    response: Response<DokumentasiSelesaiResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.dokumentasi
                            ?.sortedByDescending { it.id } ?: emptyList()

                        adapter.updateData(list)

                        if (list.isEmpty()) {
                            Toast.makeText(
                                this@LihatDokumentasi,
                                "Belum ada dokumentasi selesai",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LihatDokumentasi,
                            "Gagal memuat data (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DokumentasiSelesaiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LihatDokumentasi,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
