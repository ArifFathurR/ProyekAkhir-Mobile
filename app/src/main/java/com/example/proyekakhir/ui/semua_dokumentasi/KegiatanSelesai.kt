package com.example.proyekakhir.ui.semua_dokumentasi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.adapter.KegiatanAdapter
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.KegiatanSelesaiBinding
import com.example.proyekakhir.model.KegiatanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KegiatanSelesai : AppCompatActivity() {
    private lateinit var binding: KegiatanSelesaiBinding
    private lateinit var adapter: KegiatanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KegiatanSelesaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener { finish() }

        setupRecyclerView()
        fetchKegiatanSelesai()
    }

    private fun setupRecyclerView() {
        adapter = KegiatanAdapter(
            emptyList(),
            onUndanganClick = { /* tidak dipakai di tab selesai */ },
            onDetailClick = { id ->
                val intent = Intent(this, LihatDokumentasi::class.java)
                intent.putExtra("id", id) // kirim id dari model Kegiatan
                startActivity(intent)
            }
        )
        binding.recylerView.layoutManager = LinearLayoutManager(this)
        binding.recylerView.adapter = adapter
    }

    private fun fetchKegiatanSelesai() {
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val token = shared.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login kembali", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.instance.getKegiatanSelesai("Bearer $token")
            .enqueue(object : Callback<KegiatanResponse> {
                override fun onResponse(
                    call: Call<KegiatanResponse>,
                    response: Response<KegiatanResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.kegiatan ?: emptyList()
                        adapter.updateData(list, isSelesaiTab = true)

                        if (list.isEmpty()) {
                            Toast.makeText(
                                this@KegiatanSelesai,
                                "Belum ada kegiatan selesai",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@KegiatanSelesai,
                            "Gagal memuat data (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<KegiatanResponse>, t: Throwable) {
                    Toast.makeText(
                        this@KegiatanSelesai,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}