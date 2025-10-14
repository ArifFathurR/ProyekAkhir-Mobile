package com.example.proyekakhir

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyekakhir.databinding.DetailDokumentasiKegiatanBinding

class LihatDokumentasiDetail : AppCompatActivity() {

    private lateinit var binding: DetailDokumentasiKegiatanBinding
    private lateinit var adapter: FotoAdapterDetail_lihat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailDokumentasiKegiatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol kembali
        binding.btnKembali.setOnClickListener { finish() }

        // Ambil data dari Intent
        val judul = intent.getStringExtra("judul") ?: "Dokumentasi Kegiatan"
        val notulensi = intent.getStringExtra("notulensi") ?: "-"
        val linkZoom = intent.getStringExtra("link_zoom") ?: "-"
        val linkMateri = intent.getStringExtra("link_materi") ?: "-"
        val fotoList = intent.getStringArrayListExtra("foto_list") ?: arrayListOf()

        // Set data ke tampilan
        binding.judulRapat.text = judul
        binding.notulensi.text = notulensi
        binding.linkZoom.text = linkZoom
        binding.linkMateri.text = linkMateri

        // Klik link Zoom
        binding.linkZoom.setOnClickListener {
            if (linkZoom.startsWith("http")) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkZoom)))
            } else {
                Toast.makeText(this, "Link Zoom tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        // Klik link Materi
        binding.linkMateri.setOnClickListener {
            if (linkMateri.startsWith("http")) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkMateri)))
            } else {
                Toast.makeText(this, "Link Materi tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup RecyclerView foto
        adapter = FotoAdapterDetail_lihat(fotoList) { fotoUrl, _ ->
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(fotoUrl)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membuka foto", Toast.LENGTH_SHORT).show()
            }
        }

        binding.recyclerFoto.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerFoto.adapter = adapter

        // Klik download semua (opsional)
        binding.downloadContainer.setOnClickListener {
            Toast.makeText(this, "Fitur download semua foto belum diimplementasikan", Toast.LENGTH_SHORT).show()
        }
    }
}
