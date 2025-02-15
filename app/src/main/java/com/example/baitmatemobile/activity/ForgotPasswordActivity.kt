package com.example.baitmatemobile.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.baitmatemobile.R
import com.example.baitmatemobile.databinding.ActivityForgotPasswordBinding
import com.example.baitmatemobile.model.ErrorResponse
import com.example.baitmatemobile.model.ForgotPasswordRequest
import com.example.baitmatemobile.model.ResetPasswordRequest
import com.example.baitmatemobile.network.RetrofitClient
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {
    private var _binding: ActivityForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResetPassword.setOnClickListener{
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            if (email.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please enter both your username and email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendForgotPasswordRequest(username, email)
        }

        binding.btnVerifyOTP.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val otp = binding.otpEditText.text.toString().trim()
            val newPassword = binding.newPasswordEditText.text.toString().trim()

            if (otp.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Enter OTP and new password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendResetPasswordRequest(email, otp, newPassword)
        }
    }

    private fun sendForgotPasswordRequest(username: String, email: String) {
        val request = ForgotPasswordRequest(username, email)
        RetrofitClient.instance.forgotPassword(request).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "Check your email for reset instructions", Toast.LENGTH_SHORT).show()
                    binding.otpLayout.visibility = View.VISIBLE
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@ForgotPasswordActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ForgotPasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendResetPasswordRequest(email: String, otp: String, newPassword: String) {
        val request = ResetPasswordRequest(email, otp, newPassword)
        RetrofitClient.instance.resetPassword(request).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "Password reset successful", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Failed to reset password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ForgotPasswordActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}