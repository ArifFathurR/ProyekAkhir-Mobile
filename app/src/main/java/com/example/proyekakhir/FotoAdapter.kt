package com.example.proyekakhir

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyekakhir.R

class FotoAdapter(
    private val fotoList: MutableList<Any>,
    private val onDelete: ((Int, Any) -> Unit)? = null
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    inner class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFoto: ImageView = view.findViewById(R.id.imgFoto)
        val delete: ImageView = view.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val item = fotoList[position]
        when (item) {
            is Uri -> Glide.with(holder.itemView.context)
                .load(item)
                .centerCrop()
                .into(holder.imgFoto)
            is String -> {
                // Jika item mengandung "#", ambil bagian setelahnya (path sebenarnya)
                val path = if (item.contains("#")) item.substringAfter("#") else item
                val fullUrl = if (path.startsWith("http")) path
                else "http://10.0.2.2:8000/storage/$path"

                Glide.with(holder.itemView.context)
                    .load(fullUrl)
                    .centerCrop()
                    .into(holder.imgFoto)
            }
        }

        holder.delete.setOnClickListener { onDelete?.invoke(position, item) }
    }

    override fun getItemCount(): Int = fotoList.size

    fun addFotoUri(uri: Uri) {
        fotoList.add(uri)
        notifyItemInserted(fotoList.size - 1)
    }

    fun addFotoUrl(url: String) {
        fotoList.add(url)
        notifyItemInserted(fotoList.size - 1)
    }

    fun removeAt(position: Int) {
        if (position in fotoList.indices) {
            fotoList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getAllFotos(): List<Any> = fotoList
}
