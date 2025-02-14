package com.example.baitmatemobile.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.baitmatemobile.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("auth_token", null)

        if (authToken != null) {
           validateToken(authToken, sharedPreferences)
        } else {
            Log.e("SplashActivity", "No token found.")
            navigateToLogin()
        }
        finish()
    }

    private fun validateToken(token: String, sharedPreferences: SharedPreferences) {
        RetrofitClient.instance.validateToken(token).enqueue(object: Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                    navigateToLogin()
                }
                finish()
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@SplashActivity, "Network error", Toast.LENGTH_SHORT).show()
                navigateToLogin()
                finish()
            }
        })
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}