package com.example.proyekakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyekakhir.databinding.ItemFotoDetailBinding

class FotoAdapterDetail_lihat(
    private val fotoList: List<String>, // Sekarang langsung list URL
    private val onItemClick: (String, Int) -> Unit
) : RecyclerView.Adapter<FotoAdapterDetail_lihat.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemFotoDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fotoUrl: String, position: Int) {
            Glide.with(binding.root.context)
                .load(fotoUrl)
                .centerCrop()
                .into(binding.imgFoto)

            binding.root.setOnClickListener {
                onItemClick(fotoUrl, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFotoDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fotoList[position], position)
    }

    override fun getItemCount(): Int = fotoList.size
}
