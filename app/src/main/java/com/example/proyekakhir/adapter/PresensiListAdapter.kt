package com.example.proyekakhir.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyekakhir.databinding.ItemPresensiBinding
import com.example.proyekakhir.model.Presensi

class PresensiListAdapter(
    private val list: List<Presensi>
) : RecyclerView.Adapter<PresensiListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPresensiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Presensi) {
            binding.JudulUndangan.text = item.subKegiatan ?: "-"

            binding.tvWaktuMasuk.text = if (!item.waktuPresensi.isNullOrEmpty() && item.waktuPresensi != "-")
                "${item.waktuPresensi} WIB" else "-"

            binding.tvWaktuAbsensi.text = item.tanggal ?: "-"

            val lokasi = if (item.latitude != null && item.longitude != null)
                "${item.latitude}, ${item.longitude}"
            else "-"
            binding.tvLokasi.text = lokasi

            binding.tvKeterangan.text = item.namaKegiatan ?: "-"
            binding.tvKeterlambatan.text = ""

            // Load gambar TTD dari path storage
            val path = if (!item.ttd.isNullOrEmpty()) item.ttd else null
            val fullUrl = if (path != null) "http://10.0.2.2:8000/$path" else null

            Glide.with(binding.ivttd.context)
                .load(fullUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_edit)
                .error(android.R.drawable.ic_menu_edit)
                .into(binding.ivttd)

            // Status Kehadiran + warna badge
            val status = item.statusKehadiran ?: "Tidak Hadir"
            binding.tvStatusKehadiran.text = status
            val badgeColor = when (status.lowercase()) {
                "hadir"       -> Color.parseColor("#4CAF50")
                "terlambat"   -> Color.parseColor("#FF9800")
                "tidak hadir" -> Color.parseColor("#F44336")
                else          -> Color.parseColor("#9E9E9E")
            }
            binding.tvStatusKehadiran.setBackgroundColor(badgeColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPresensiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}