package com.example.proyekakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyekakhir.databinding.ItemPresensiKegiatanBinding
import com.example.proyekakhir.model.Kegiatan

class PresensiAdapter(
    private var kegiatanList: List<Kegiatan>,
    private val onPresensiClick: (Kegiatan) -> Unit
) : RecyclerView.Adapter<PresensiAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPresensiKegiatanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(kegiatan: Kegiatan) {
            binding.tanggal.text = kegiatan.tanggal
            binding.judulRapat.text = kegiatan.sub_kegiatan
            binding.btnIsiPresensi.setOnClickListener {
                onPresensiClick(kegiatan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPresensiKegiatanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = kegiatanList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(kegiatanList[position])
    }

    fun updateData(newData: List<Kegiatan>) {
        kegiatanList = newData
        notifyDataSetChanged()
    }
}
