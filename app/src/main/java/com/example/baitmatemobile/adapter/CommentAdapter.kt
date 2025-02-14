package com.example.baitmatemobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baitmatemobile.R
import com.example.baitmatemobile.model.Comment

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        private val ivLike: ImageView = itemView.findViewById(R.id.ivLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)

        fun bind(comment: Comment) {
            tvUsername.text = comment.user?.username ?: "Unknown"
            tvComment.text = comment.comment ?: ""
            tvLikeCount.text = comment.likeCount?.toString() ?: "0"

            ivLike.setOnClickListener {
                val isCurrentlyLiked = (ivLike.tag as? Boolean) == true
                val newLike = !isCurrentlyLiked
                // ...
                updateLikeUI(newLike, (comment.likeCount ?: 0) + (if (newLike) 1 else -1))
            }
        }

        private fun updateLikeUI(isLiked: Boolean, newCount: Int) {
            if (isLiked) {
                ivLike.setImageResource(R.drawable.ic_like_filled)
            } else {
                ivLike.setImageResource(R.drawable.ic_like_outline)
            }
            tvLikeCount.text = newCount.toString()
            ivLike.tag = isLiked
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem&&
                    oldItem.likeCount == newItem.likeCount //&&
                    //oldItem.likedByCurrentUser == newItem.likedByCurrentUser
        }
    }
}
