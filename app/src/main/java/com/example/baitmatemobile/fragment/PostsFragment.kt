package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.baitmatemobile.activity.PostDetailActivity
import com.example.baitmatemobile.adapter.PostAdapter
import com.example.baitmatemobile.databinding.FragmentPostsBinding
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch


class PostsFragment : Fragment() {
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getLong("userId", -1)
        initRecyclerView(userId)
        loadPosts(userId)

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPosts(userId)
        }
    }

    private fun initRecyclerView(userId: Long) {
        postAdapter = PostAdapter(userId) {
            clickedPost ->
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra("postId", clickedPost.id)
            startActivity(intent)
        }

        binding.userPosts.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = postAdapter
        }
    }

    private fun loadPosts(userId: Long) {
        lifecycleScope.launch {
            try {
                val posts = RetrofitClient.instance.getPostsByUserId(userId)
                postAdapter.submitList(posts)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Load posts failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
