package com.example.baitmatemobile.fragment
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.PostDetailActivity
import com.example.baitmatemobile.adapter.PostAdapter
import com.example.baitmatemobile.databinding.FragmentDiscoverBinding
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter  // RecyclerView Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getLong("userId", -1)
        initRecyclerView(userId)
        loadPosts()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun initRecyclerView(userId: Long) {
        postAdapter = PostAdapter(userId) { clickedPost ->

            val postDetailFragment = PostDetailFragment()
            val args = Bundle().apply {
                clickedPost.id?.let { putLong("postId", it) } // Pass the clickedPost.id
            }
            postDetailFragment.arguments = args

            requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, postDetailFragment)
                .addToBackStack("discover_to_post_detail")
                .commit()
        }

        binding.rvPosts.apply {
            // 两列瀑布流
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = postAdapter
        }
    }

    private fun loadPosts() {
        lifecycleScope.launch {
            try {
                val posts = RetrofitClient.instance.getAllPosts()
                val filteredPosts = posts.filter { it.postStatus == "approved" || it.postStatus == "petition" }
                postAdapter.submitList(filteredPosts)

                binding.swipeRefreshLayout.isRefreshing = false
            } catch (e: Exception) {
                //e.printStackTrace()
                Log.e("MY_TAG", "Request Error：${e.message}", e)
                Toast.makeText(requireContext(), "Load posts failed", Toast.LENGTH_SHORT).show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

