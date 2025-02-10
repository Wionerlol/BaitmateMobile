package com.example.baitmatemobile.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baitmatemobile.R

data class ImageItem(val uri: Uri, var isSelected: Boolean)

class ChooseImagesAdapter(
    private val images: List<ImageItem>,
    private val onItemCheckChanged: (position: Int, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<ChooseImagesAdapter.ChooseImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseImagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_choose, parent, false)
        return ChooseImagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChooseImagesViewHolder, position: Int) {
        val imageItem = images[position]
        holder.bind(imageItem, onItemCheckChanged)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ChooseImagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val checkIcon: ImageView = itemView.findViewById(R.id.ivCheckIcon)

        fun bind(item: ImageItem, onItemCheckChanged: (position: Int, isChecked: Boolean) -> Unit) {
            // 加载图片，可以使用Glide/Picasso等
            Glide.with(ivPhoto.context).load(item.uri).into(ivPhoto)

            // 根据 item.isSelected 显示/隐藏对勾
            checkIcon.visibility = if (item.isSelected) View.VISIBLE else View.INVISIBLE

            // 点击对勾/点击图片都可以切换选中状态
            itemView.setOnClickListener {
                item.isSelected = !item.isSelected
                checkIcon.visibility = if (item.isSelected) View.VISIBLE else View.INVISIBLE
                onItemCheckChanged(adapterPosition, item.isSelected)
            }
        }
    }
}
