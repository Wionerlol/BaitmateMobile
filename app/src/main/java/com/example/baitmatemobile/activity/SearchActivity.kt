package com.example.baitmatemobile.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baitmatemobile.R
import com.example.baitmatemobile.databinding.ActivitySearchBinding
import com.example.baitmatemobile.viewmodel.SearchViewModel
import com.example.baitmatemobile.adapter.SearchAdapter

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchAdapter
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        binding.activity = this
        binding.searchQuery = viewModel.searchQuery.toString()

        adapter = SearchAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    fun performSearch(query: String) {
        val searchResults = listOf(
            "Fishing Spot 1",
            "Fishing Spot 2",
            "Fishing Spot 3"
        )
        adapter.updateData(searchResults)
    }
}
