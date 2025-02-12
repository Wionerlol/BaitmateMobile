package com.example.baitmatemobile.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.PostDetailActivity
import com.example.baitmatemobile.model.CommentRedDot
import com.example.baitmatemobile.model.LikeRedDot
import com.example.baitmatemobile.model.RedDot

class RedDotAdapter(context: Context, private val redDots: List<RedDot>) :
    ArrayAdapter<RedDot>(context, R.layout.notification_item, redDots) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false)
        val notification = redDots[position]

        val username = view.findViewById<TextView>(R.id.notification_username)
        val postTitle = view.findViewById<TextView>(R.id.notification_post_title)
        val commentText = view.findViewById<TextView>(R.id.notification_comment_text)
        val time = view.findViewById<TextView>(R.id.notification_time)

        username.text = notification.sender?.username
        postTitle.text = notification.post?.postTitle
        time.text = notification.time

        when (notification) {
            is CommentRedDot -> {
                commentText.text = notification.commentText
            }
            is LikeRedDot -> {
                commentText.text = "liked your post"
            }
        }

//        username.setOnClickListener {
//            val intent = Intent(context, ProfileFragment::class.java)
//            intent.putExtra("userId", notification.sender.id)
//            context.startActivity(intent)
//        }

        postTitle.setOnClickListener {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("postId", notification.post?.id)
            context.startActivity(intent)
        }

        return view
    }
}
