package com.example.baitmatemobile.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.baitmatemobile.databinding.ActivityUpdateProfileBinding
import com.example.baitmatemobile.viewmodel.UserViewModel
import org.json.JSONObject

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        viewModel.loadCurrentUser()

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            binding.etUsername.setText(user.username)
            binding.etEmail.setText(user.email)
            binding.etPhone.setText(user.phoneNumber)
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfile() {
        val currentUser = viewModel.user.value ?: return
        val updatedUser = currentUser.copy(
            username = binding.etUsername.text.toString(),
            email = binding.etEmail.text.toString(),
            phoneNumber = binding.etPhone.text.toString()
        )
        viewModel.updateProfile(updatedUser)
    }
}
