package com.example.proyekakhir

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyekakhir.databinding.ItemDokumentasiKegiatanBinding
import com.example.proyekakhir.model.DokumentasiSelesai
import com.squareup.picasso.Picasso

class LihatDokumentasiAdapter :
    RecyclerView.Adapter<LihatDokumentasiAdapter.ViewHolder>() {

    private var dokumentasiList: MutableList<DokumentasiSelesai> = mutableListOf()

    inner class ViewHolder(private val binding: ItemDokumentasiKegiatanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DokumentasiSelesai) {
            // Set judul dan notulensi
            binding.judulRapat.text = item.subKegiatan
            binding.textView.text = item.notulensi ?: "Tidak ada notulensi"

            // Tampilkan foto pertama jika ada
            if (item.fotoDokumentasi.isNotEmpty()) {
                Picasso.get()
                    .load(item.fotoDokumentasi[0])
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(binding.gambar)
            } else {
                binding.gambar.setImageResource(R.drawable.default_image)
            }

            // Klik untuk membuka halaman detail
            binding.btnDetail.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, LihatDokumentasiDetail::class.java).apply {
                    putExtra("judul", item.subKegiatan ?: "Dokumentasi Kegiatan")
                    putExtra("notulensi", item.notulensi ?: "-")
                    putExtra("link_zoom", item.linkZoom ?: "-")
                    putExtra("link_materi", item.linkMateri ?: "-")
                    putStringArrayListExtra("foto_list", ArrayList(item.fotoDokumentasi))
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDokumentasiKegiatanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dokumentasiList[position])
    }

    override fun getItemCount(): Int = dokumentasiList.size

    fun updateData(newList: List<DokumentasiSelesai>) {
        dokumentasiList.clear()
        dokumentasiList.addAll(newList)
        notifyDataSetChanged()
    }
}
