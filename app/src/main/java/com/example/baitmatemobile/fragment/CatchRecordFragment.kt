package com.example.baitmatemobile.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.baitmatemobile.adapter.CatchRecordAdapter
import com.example.baitmatemobile.databinding.FragmentCatchRecordBinding
import com.example.baitmatemobile.network.RetrofitClient
import kotlinx.coroutines.launch

class CatchRecordFragment : Fragment() {
    private var _binding: FragmentCatchRecordBinding? = null
    private val binding get() = _binding!!
    private var userId: Long = -1

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: Long): CatchRecordFragment {
            val fragment = CatchRecordFragment()
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
        _binding = FragmentCatchRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        loadCatchRecords()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadCatchRecords()
        }
    }

    private fun initRecyclerView() {
        binding.userCatchRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.userCatchRecords.adapter = CatchRecordAdapter(emptyList())
    }

    private fun loadCatchRecords() {
        lifecycleScope.launch {
            try {
                val catchRecords = RetrofitClient.instance.getCatchRecordsByUserId(userId)
                Log.d("CatchRecordFragment", "Received response $catchRecords")
                binding.userCatchRecords.adapter = CatchRecordAdapter(catchRecords)
            } catch (e: Exception) {
                Log.e("CatchRecordFragment","Load catch records failed: ${e.message}")
                Toast.makeText(requireContext(), "Load catch records failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}