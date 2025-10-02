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
    private val onDeleteClick: (Dokumentasi, Int) -> Unit // kirim id + posisi
) : RecyclerView.Adapter<DokumentasiSayaAdapter.ViewHolder>() {

    private val baseStorageUrl = "http://10.0.2.2:8000/storage/"

    inner class ViewHolder(private val binding: ItemDokumentasiKegiatanSayaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dokumentasi: Dokumentasi, position: Int) {
            binding.judulRapat.text = dokumentasi.undangan?.judul ?: "-"
            binding.textView.text = dokumentasi.notulensi ?: "-"

            if (!dokumentasi.fotoDokumentasi.isNullOrEmpty()) {
                val fotoPath = dokumentasi.fotoDokumentasi[0].foto
                val fullUrl = "$baseStorageUrl$fotoPath"

                Picasso.get()
                    .load(fullUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(binding.gambar)
            } else {
                binding.gambar.setImageResource(R.drawable.default_image)
            }

            binding.btnEdit.setOnClickListener { onEditClick(dokumentasi) }
            binding.btnDelete.setOnClickListener { onDeleteClick(dokumentasi, position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDokumentasiKegiatanSayaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dokumentasiList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dokumentasiList[position], position)
    }

    fun updateData(newList: List<Dokumentasi>) {
        dokumentasiList.clear()
        dokumentasiList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in dokumentasiList.indices) {
            dokumentasiList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dokumentasiList.size)
        }
    }
}
