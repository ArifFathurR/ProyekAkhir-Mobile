package com.example.proyekakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyekakhir.databinding.ItemKegiatanBinding
import com.example.proyekakhir.model.Kegiatan

class KegiatanAdapter(
    private var list: List<Kegiatan>,
    private val onUndanganClick: (String) -> Unit,
    private val onDetailClick: (Int) -> Unit
) : RecyclerView.Adapter<KegiatanAdapter.KegiatanViewHolder>() {

    private var isSelesai = false

    inner class KegiatanViewHolder(val binding: ItemKegiatanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KegiatanViewHolder {
        val binding = ItemKegiatanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KegiatanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KegiatanViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tanggal.text = item.tanggal
            subKegiatan.text = item.sub_kegiatan

            if (isSelesai) {
                btnLihat.text = "Detail"
                btnLihat.setOnClickListener {
                    onDetailClick(item.id)
                }
            } else {
                btnLihat.text = "Lihat Detail"
                btnLihat.setOnClickListener {
                    onUndanganClick(item.file_undangan)
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Kegiatan>, isSelesaiTab: Boolean) {
        list = newList
        isSelesai = isSelesaiTab
        notifyDataSetChanged()
    }
}