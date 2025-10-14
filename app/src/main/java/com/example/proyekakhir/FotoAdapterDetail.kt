package com.example.proyekakhir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyekakhir.api.ApiClient
import com.example.proyekakhir.databinding.ItemFotoBinding
import com.example.proyekakhir.model.FotoDetail

class FotoAdapterDetail(
    private var fotoList: List<FotoDetail>,
    private val onFotoClick: (FotoDetail, Int) -> Unit
) : RecyclerView.Adapter<FotoAdapterDetail.FotoViewHolder>() {

    inner class FotoViewHolder(val binding: ItemFotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val binding = ItemFotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotoList[position]
        val fullImageUrl = ApiClient.BASE_IMAGE_URL + foto.fileFoto

        with(holder.binding) {
            // Load image dengan Glide
            Glide.with(root.context)
                .load(fullImageUrl)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(imgFoto)

            // Click listener untuk preview foto
            root.setOnClickListener {
                onFotoClick(foto, position)
            }
        }
    }

    override fun getItemCount(): Int = fotoList.size

    fun updateData(newList: List<FotoDetail>) {
        fotoList = newList
        notifyDataSetChanged()
    }
}