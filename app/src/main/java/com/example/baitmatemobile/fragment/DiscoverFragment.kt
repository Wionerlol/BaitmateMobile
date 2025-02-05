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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.PostDetailActivity
import com.example.baitmatemobile.adapter.PostAdapter
import com.example.baitmatemobile.api.ApiClient
import com.example.baitmatemobile.databinding.FragmentDiscoverBinding
import kotlinx.coroutines.launch

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
    }

    private fun initRecyclerView(userId: Long) {
        postAdapter = PostAdapter(userId) { clickedPost ->
            // 点击图片或者整块区域进入详情
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra("postId", clickedPost.id)
            startActivity(intent)
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
                val posts = ApiClient.apiService.getAllPosts()
                postAdapter.submitList(posts)
            } catch (e: Exception) {
                //e.printStackTrace()
                Log.e("MY_TAG", "请求出错：${e.message}", e)
                Toast.makeText(requireContext(), "加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

