package com.example.baitmatemobile.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.baitmatemobile.databinding.ActivityRegisterBinding
import com.example.baitmatemobile.model.RegisterRequest
import com.example.baitmatemobile.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val ageString = binding.etAge.text.toString().trim()
        val gender = binding.etGender.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username cannot be blank"
                return
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password cannot be blank"
                return
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email is mandatory"
                return
            }
            ageString.isEmpty() -> {
                binding.etAge.error = "Age is required"
                return
            }
        }

        val age = ageString.toInt()
        val registerRequest = RegisterRequest(username, password, phoneNumber, email, age, gender, address)

        Log.d("RegisterActivity", "Sending Register Request: $registerRequest")

        RetrofitClient.instance.register(registerRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("RegisterActivity", "Response Success: ${response.body()?.string()}")
                    Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "An error occurred"
                    Log.e("RegisterActivity", "Error Response Code: ${response.code()}, Body: $errorBody")
                    Toast.makeText(this@RegisterActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("RegisterActivity", "Network Failure: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
