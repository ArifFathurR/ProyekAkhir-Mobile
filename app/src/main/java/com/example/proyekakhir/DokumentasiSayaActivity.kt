package com.example.proyekakhir

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

        binding.btnKembali.setOnClickListener { finish() }
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
                            onDeleteClick = { dokumentasi, position ->
                                confirmDelete(token, dokumentasi.id, position)
                            }
                        )
                        binding.recylerView.adapter = adapter
                    } else {
                        Toast.makeText(this@DokumentasiSayaActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DokumentasiResponse>, t: Throwable) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@DokumentasiSayaActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmDelete(token: String, id: Int, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin menghapus dokumentasi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteDokumentasi(token, id, position)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteDokumentasi(token: String, id: Int, position: Int) {
        ApiClient.instance.deleteDokumentasi("Bearer $token", id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DokumentasiSayaActivity, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        adapter.removeItem(position)
                    } else {
                        Toast.makeText(this@DokumentasiSayaActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@DokumentasiSayaActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
