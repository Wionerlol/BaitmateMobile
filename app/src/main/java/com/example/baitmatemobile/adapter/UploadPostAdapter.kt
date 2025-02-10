package com.example.baitmatemobile.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baitmatemobile.R

/**
 * 预览图片用的Adapter
 */
class PreviewImagesAdapter(private val imageUris: List<Uri>) :
    RecyclerView.Adapter<PreviewImagesAdapter.PreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preview_image, parent, false)
        return PreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        holder.bind(imageUris[position])
    }

    override fun getItemCount(): Int = imageUris.size

    class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPreview: ImageView = itemView.findViewById(R.id.ivPreview)
        fun bind(uri: Uri) {
            Glide.with(ivPreview.context).load(uri).into(ivPreview)
        }
    }
}