package com.example.baitmatemobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baitmatemobile.R
import com.example.baitmatemobile.model.Image
import com.example.baitmatemobile.network.RetrofitClient

class ImagePagerAdapter(private val images: List<Image>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_image_pager, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // 1. 拿到后端返回的 imageOid
        val imageOid = images[position].image

        // 2. 如果你有后端下载图片的接口，可以这样拼接
        //    比如 GET /api/images/oid/{oid} 返回图片流
        //    这里演示一个假设的 url
        val imageUrl = RetrofitClient.retrofit.baseUrl().toString() + "posts/oid/$imageOid"

        // 3. 用 Glide 加载
        Glide.with(holder.itemView)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder) // 占位图
            .error(R.drawable.error_image) // 错误图
            .into(holder.ivImage)
    }

    override fun getItemCount(): Int = images.size
}

