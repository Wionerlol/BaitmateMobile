// LoginFragment.kt
package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.MainActivity
import com.example.baitmatemobile.databinding.FragmentLoginBinding
import com.example.baitmatemobile.model.LoginRequest
import com.example.baitmatemobile.model.LoginResponse
import com.example.baitmatemobile.network.RetrofitClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.btnLogin

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if(username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()
                    val userId = loginResponse?.id
                    val usernameResponse = loginResponse?.username

                    // Save login status to SharedPreferences
                    saveLoginStatus(userId, usernameResponse)
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveLoginStatus(userId: Long?, username: String?) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        if (userId != null) {
            editor.putLong("userId", userId)
        }
        editor.putString("username", username)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
