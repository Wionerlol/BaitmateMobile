package com.example.baitmatemobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableField

class SearchViewModel : ViewModel() {
    val searchQuery = ObservableField<String>("") // ✅ 绑定 EditText
}
