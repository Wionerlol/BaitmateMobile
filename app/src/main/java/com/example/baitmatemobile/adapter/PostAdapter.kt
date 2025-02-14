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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PostAdapter(
    private val userId: Long,
    private val lifecycleScope: CoroutineScope,
    private val onItemClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_staggered, parent, false)
        return PostViewHolder(userId, lifecycleScope, view, onItemClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
        holder.loadLikeState(post)
    }

    class PostViewHolder(
        private val userId: Long,
        private val lifecycleScope: CoroutineScope,
        itemView: View,
        private val onItemClick: (Post) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val ivLike: ImageView = itemView.findViewById(R.id.ivLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val IMAGE_BASE_URL = RetrofitClient.retrofit.baseUrl().toString() + "posts/oid/"

        fun bind(post: Post) {
            tvTitle.text = post.postTitle ?: ""
            tvUsername.text = post.user?.username ?: "Unknown"
            tvLikeCount.text = post.likeCount?.toString() ?: "0"
            loadPostImage(post)
            ivLike.setImageResource(if (post.likedByCurrentUser == true) R.drawable.ic_like_filled else R.drawable.ic_like_outline)
            ivLike.tag = post.likedByCurrentUser

            ivPostImage.setOnClickListener { onItemClick.invoke(post) }
            ivLike.setOnClickListener { toggleLike(post) }
        }

        fun loadLikeState(post: Post) {
            lifecycleScope.launch {
                try {
                    val latestPost = RetrofitClient.instance.getPostByIdWithUser(post.id ?: return@launch, userId)
                    updateLikeUI(latestPost.likedByCurrentUser ?: false, latestPost.likeCount ?: 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun loadPostImage(post: Post) {
            val firstImageOid = post.images?.firstOrNull()?.image
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
        }

        private fun toggleLike(post: Post) {
            lifecycleScope.launch {
                try {
                    val updatedPost = RetrofitClient.instance.toggleLike(post.id ?: return@launch, userId)
                    updateLikeUI(updatedPost.likedByCurrentUser ?: false, updatedPost.likeCount ?: 0)
                    post.likeCount = updatedPost.likeCount
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun updateLikeUI(isLiked: Boolean, newCount: Int) {
            ivLike.setImageResource(if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline)
            tvLikeCount.text = newCount.toString()
            ivLike.tag = isLiked
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem &&
                    oldItem.likeCount == newItem.likeCount &&
                    oldItem.likedByCurrentUser == newItem.likedByCurrentUser
        }
    }
}


fun Context.lifecycleOwner(): LifecycleOwner? = when(this) {
    is LifecycleOwner -> this
    is ContextWrapper -> baseContext.lifecycleOwner()
    else -> null
}

