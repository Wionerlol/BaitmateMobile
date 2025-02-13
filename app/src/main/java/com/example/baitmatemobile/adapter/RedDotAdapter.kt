package com.example.baitmatemobile.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.baitmatemobile.R
import com.example.baitmatemobile.fragment.OthersProfileFragment
import com.example.baitmatemobile.fragment.PostDetailFragment
import com.example.baitmatemobile.fragment.ProfileFragment
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

        username.setOnClickListener {
            val viewedUserId = notification.sender?.id
            Log.d("Profile Fragment", "Clicked userId: $viewedUserId")

            if (viewedUserId == null) {
                Toast.makeText(context, "User ID is null", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPrefs.getLong("userId", -1)

            val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager
            if (fragmentManager == null) {
                Log.e("Profile Fragment", "FragmentManager is null")
                return@setOnClickListener
            }

            val fragment = if (viewedUserId == userId) {
                ProfileFragment()
            } else {
                OthersProfileFragment.newInstance(viewedUserId)
            }

            fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .addToBackStack("profile_navigation")
                .commit()
        }


        postTitle.setOnClickListener {
            val postDetailFragment = PostDetailFragment().apply {
                arguments = Bundle().apply {
                    notification.post?.id?.let { it1 -> putLong("postId", it1) }
                }
            }

            val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager
            fragmentManager?.beginTransaction()
                ?.add(R.id.fragment_container, postDetailFragment)  // `fragment_container` 是你的 Fragment 容器 ID
                ?.addToBackStack(null)  // 添加到返回栈，允许用户按返回键回到上一个 Fragment
                ?.commit()
        }



        return view
    }
}
