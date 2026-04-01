package com.example.proyekakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.adapter.PresensiAdapter
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.auth.LoginActivity
import com.example.proyekakhir.databinding.HistoryPresensiBinding
import com.example.proyekakhir.model.PresensiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryPresensi : AppCompatActivity() {

    private lateinit var binding: HistoryPresensiBinding
    private lateinit var adapter: PresensiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HistoryPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener { finish() }

        setupRecyclerView()
        fetchRiwayatPresensi()
    }

    private fun setupRecyclerView() {
        adapter = PresensiAdapter(emptyList())
        binding.recylerView.layoutManager = LinearLayoutManager(this)
        binding.recylerView.adapter = adapter
    }

    private fun fetchRiwayatPresensi() {
        val shared = getSharedPreferences("APP", MODE_PRIVATE)
        val token = shared.getString("TOKEN", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login kembali", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.instance.getRiwayatPresensi("Bearer $token")
            .enqueue(object : Callback<PresensiResponse> {
                override fun onResponse(
                    call: Call<PresensiResponse>,
                    response: Response<PresensiResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.data ?: emptyList()

                        adapter = PresensiAdapter(list)
                        binding.recylerView.adapter = adapter

                        if (list.isEmpty()) {
                            Toast.makeText(
                                this@HistoryPresensi,
                                "Belum ada riwayat presensi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@HistoryPresensi,
                            "Gagal memuat data (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PresensiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@HistoryPresensi,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}