package com.example.baitmatemobile.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.RedDotAdapter
import com.example.baitmatemobile.model.RedDot
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch


class MessageFragment : Fragment() {
    private lateinit var redDotListView: ListView
    private lateinit var adapter: RedDotAdapter
    private val redDotList = mutableListOf<RedDot>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        redDotListView = view.findViewById(R.id.notification_list)
        adapter = RedDotAdapter(requireContext(), redDotList)
        redDotListView.adapter = adapter

        loadNotifications()
        return view
    }

    private fun loadNotifications() {
        val sharedPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getLong("userId", -1)

        lifecycleScope.launch {
            try {
                val redDots = RetrofitClient.instance.getRedDots(userId)
                    .map { it.toRedDot() }

                redDotList.apply {
                    clear()
                    addAll(redDots)
                }

                adapter.notifyDataSetChanged()

                redDots.forEach { redDot ->
                    launch {
                        val user = RetrofitClient.instance.getUserById(redDot.senderId)
                        val post = RetrofitClient.instance.getPostById(redDot.postId)
                        redDot.sender = user
                        redDot.post = post
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        }

    }
}

