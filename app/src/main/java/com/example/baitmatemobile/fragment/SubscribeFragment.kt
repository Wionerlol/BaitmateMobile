package com.example.baitmatemobile.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.baitmatemobile.R
import com.example.baitmatemobile.adapter.PostAdapter
import com.example.baitmatemobile.databinding.FragmentSubscribeBinding
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch


class SubscribeFragment : Fragment() {

    private var _binding: FragmentSubscribeBinding? = null
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscribeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getLong("userId", -1)
        initRecyclerView(userId)
        loadFollowingPosts(userId)

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadFollowingPosts(userId)
        }
    }

    private fun initRecyclerView(userId: Long) {
        postAdapter = PostAdapter(userId, viewLifecycleOwner.lifecycleScope) { clickedPost ->
            val postDetailFragment = PostDetailFragment()
            val args = Bundle().apply {
                clickedPost.id?.let { putLong("postId", it) }
            }
            postDetailFragment.arguments = args

            requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, postDetailFragment)
                .addToBackStack("subscribe_to_post_detail")
                .commit()
        }

        binding.FollowingPosts.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = postAdapter
        }
    }

    private fun loadFollowingPosts(userId: Long) {
        lifecycleScope.launch {
            try {
                val posts = RetrofitClient.instance.getFollowingPosts(userId)
                val filteredPosts = posts.filter { it.postStatus == "approved" || it.postStatus == "petition" }
                postAdapter.submitList(filteredPosts)

                binding.swipeRefreshLayout.isRefreshing = false
            } catch (e: Exception) {
                Log.e("MY_TAG", "Request Error：${e.message}", e)
                Toast.makeText(requireContext(), "Load following posts failed", Toast.LENGTH_SHORT).show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

