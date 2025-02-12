package com.example.baitmatemobile.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.ChooseImagesActivity
import com.example.baitmatemobile.activity.SearchActivity
import com.example.baitmatemobile.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 默认显示 DiscoverFragment
        replaceFragment(DiscoverFragment())

        // 🔹 绑定顶部导航 (Following, Discover, Help)
        binding.btnSubscribe.setOnClickListener { replaceFragment(SubscribeFragment()) }
        binding.btnExplore.setOnClickListener { replaceFragment(DiscoverFragment()) }
        binding.btnAssist.setOnClickListener { replaceFragment(AssistFragment()) }

        // 🔍 绑定搜索按钮点击事件
        binding.btnSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

       binding.btnPost.setOnClickListener {
           binding.btnPost.setOnClickListener {
               // 跳转到选择图片Activity
               val intent = Intent(requireContext(), ChooseImagesActivity::class.java)
               startActivity(intent)
           }
        }

        return binding.root
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 防止内存泄漏
    }
}
