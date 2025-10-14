package com.example.proyekakhir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyekakhir.databinding.DokumentasiKegiatanBinding

class LihatDokumentasi : AppCompatActivity() {
    private lateinit var binding: DokumentasiKegiatanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DokumentasiKegiatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKembali.setOnClickListener { finish() }
    }
}
