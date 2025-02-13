package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.Intent
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
import com.example.baitmatemobile.activity.PostDetailActivity
import com.example.baitmatemobile.adapter.PostAdapter
import com.example.baitmatemobile.databinding.FragmentPostsBinding
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch


class PostsFragment : Fragment() {
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private var userId: Long = -1

    private lateinit var postAdapter: PostAdapter

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: Long): PostsFragment {
            val fragment = PostsFragment()
            val args = Bundle()
            args.putLong(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getLong(ARG_USER_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        loadPosts()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun initRecyclerView() {
        postAdapter = PostAdapter(userId) {
            clickedPost ->

            val postDetailFragment = PostDetailFragment()
            val args = Bundle().apply {
                clickedPost.id?.let { putLong("postId", it) } // Pass the clickedPost.id
            }
            postDetailFragment.arguments = args

            requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, postDetailFragment)
                .addToBackStack("discover_to_post_detail")
                .commit()
            /*
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra("postId", clickedPost.id)
            startActivity(intent)

             */
        }

        binding.userPosts.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = postAdapter
        }
    }

    private fun loadPosts() {
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
