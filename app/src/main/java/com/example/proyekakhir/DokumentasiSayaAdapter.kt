package com.example.proyekakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyekakhir.databinding.ItemDokumentasiKegiatanSayaBinding
import com.example.proyekakhir.model.Dokumentasi
import com.squareup.picasso.Picasso

class DokumentasiSayaAdapter(
    private var dokumentasiList: MutableList<Dokumentasi>,
    private val onEditClick: (Dokumentasi) -> Unit,
    private val onDeleteClick: (Dokumentasi) -> Unit
) : RecyclerView.Adapter<DokumentasiSayaAdapter.ViewHolder>() {

    // URL dasar storage Laravel
    private val baseStorageUrl = "http://10.0.2.2:8000/storage/"

    inner class ViewHolder(private val binding: ItemDokumentasiKegiatanSayaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dokumentasi: Dokumentasi) {
            // Tampilkan judul undangan jika ada
            binding.judulRapat.text = dokumentasi.undangan?.judul ?: "-"

            // Tampilkan deskripsi kegiatan atau notulensi
            binding.textView.text = dokumentasi.notulensi ?: "-"

            // Load gambar pertama dari fotoDokumentasi
            if (!dokumentasi.fotoDokumentasi.isNullOrEmpty()) {
                val fotoPath = dokumentasi.fotoDokumentasi[0].foto // misal: foto_dokumentasi/xxx.png
                val fullUrl = "$baseStorageUrl$fotoPath"

                Picasso.get()
                    .load(fullUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(binding.gambar)
            } else {
                binding.gambar.setImageResource(R.drawable.default_image)
            }

            // Tombol edit dan delete
            binding.btnEdit.setOnClickListener { onEditClick(dokumentasi) }
            binding.btnDelete.setOnClickListener { onDeleteClick(dokumentasi) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDokumentasiKegiatanSayaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dokumentasiList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dokumentasiList[position])
    }

    fun updateData(newList: List<Dokumentasi>) {
        dokumentasiList.clear()
        dokumentasiList.addAll(newList)
        notifyDataSetChanged()
    }
}
