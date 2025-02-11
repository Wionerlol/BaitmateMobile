package com.example.baitmatemobile.adapter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baitmatemobile.R
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch

class PostAdapter(
    private val userId: Long,
    private val onItemClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_staggered, parent, false)
        return PostViewHolder(userId,view, onItemClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val userId: Long,
        itemView: View,
        private val onItemClick: (Post) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private lateinit var sharedPrefs: SharedPreferences

        private val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val ivLike: ImageView = itemView.findViewById(R.id.ivLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)

        // 你可能在 ApiClient 里定义了一个 BASE_URL，这里手动写死示例
        private val IMAGE_BASE_URL = RetrofitClient.retrofit.baseUrl().toString() + "posts/oid/"

        fun bind(post: Post) {
            // 设置标题
            tvTitle.text = post.postTitle ?: ""
            // 设置用户名
            tvUsername.text = post.user?.username ?: "Unknown"
            // likeCount
            tvLikeCount.text = post.likeCount?.toString() ?: "0"

            // 加载第一张图片 (取 images 列表第一个)
            val firstImageOid = post.images?.firstOrNull()?.image  // image 是 Long?
            if (firstImageOid != null) {
                val imageUrl = IMAGE_BASE_URL + firstImageOid
                Glide.with(itemView)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(ivPostImage)
            } else {
                ivPostImage.setImageResource(R.drawable.placeholder)
            }

            itemView.context.lifecycleOwner()?.lifecycleScope?.launch {
                try {
                    val latestPost = RetrofitClient.instance.getPostByIdWithUser(post.id ?: return@launch, userId)
                    val isLiked = latestPost.likedByCurrentUser
                    val newCount = latestPost.likeCount ?: 0

                    if (isLiked) {
                        ivLike.setImageResource(R.drawable.ic_like_filled) // 红色like
                    } else {
                        ivLike.setImageResource(R.drawable.ic_like_outline)
                    }
                    tvLikeCount.text = newCount.toString()
                    ivLike.tag = isLiked

                    // **同步更新 post 数据**
                    post.likeCount = newCount
                    post.likedByCurrentUser = isLiked

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 点击图片 -> 详情
            ivPostImage.setOnClickListener {
                onItemClick.invoke(post)
            }

            // 处理“点赞”按钮
            ivLike.setOnClickListener {

                // 发起异步请求，调用 toggleLike
                itemView.context.lifecycleOwner()?.lifecycleScope?.launch {
                    try {
                        val updatedPost = RetrofitClient.instance.toggleLike(
                            postId = post.id ?: return@launch,
                            userId = userId
                        )
                        // 后端返回最新的 likeCount
                        val newCount = updatedPost.likeCount ?: 0

                        val isLiked = updatedPost.likedByCurrentUser

                        // 更新UI
                        updateLikeUI(isLiked, newCount)

                        // 同时更新 post 的 likeCount, 以免滚动回来后数字又变
                        post.likeCount = newCount

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private fun updateLikeUI(isLiked: Boolean, newCount: Int) {
            // 更新图标与计数
            if (isLiked) {
                ivLike.setImageResource(R.drawable.ic_like_filled) // 红色like
            } else {
                ivLike.setImageResource(R.drawable.ic_like_outline)
            }
            tvLikeCount.text = newCount.toString()
            ivLike.tag = isLiked
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}

// 扩展：获取LifecycleOwner
fun Context.lifecycleOwner(): LifecycleOwner? = when(this) {
    is LifecycleOwner -> this
    is ContextWrapper -> baseContext.lifecycleOwner()
    else -> null
}

