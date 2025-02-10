package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.LoginActivity
import com.example.baitmatemobile.model.User
import com.example.baitmatemobile.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ProfileFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userInfoTextView: TextView
    private lateinit var followingCountTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var actionButton: Button
    private lateinit var btnLogout: Button

    private var viewedUserId: Long? = null
    private var isFollowing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        userNameTextView = view.findViewById(R.id.userName)
        userInfoTextView = view.findViewById(R.id.userInfo)
        followingCountTextView = view.findViewById(R.id.followingCount)
        followersCountTextView = view.findViewById(R.id.followersCount)
        actionButton = view.findViewById(R.id.actionButton)
        btnLogout = view.findViewById(R.id.btnLogout)

        loadUserProfile()

        btnLogout.setOnClickListener { logout() }

        return view
    }

    private fun loadUserProfile() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val loggedInUserId = sharedPreferences.getLong("user_id", -1L)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No valid session found", Toast.LENGTH_SHORT).show()
            return
        }

        if (viewedUserId == null || viewedUserId == loggedInUserId) {
            RetrofitClient.instance.getUserProfile("Bearer $token").enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        updateUI(user, loggedInUserId, token)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            RetrofitClient.instance.getUserProfileById("Bearer $token", viewedUserId!!).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        updateUI(user, loggedInUserId, token)
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUI(user: User, loggedInUserId: Long, token: String) {
        userNameTextView.text = user.username ?: "Unknown"
        userInfoTextView.text = user.email ?: "No Email"
        followersCountTextView.text = "${user.followersCount} Followers"
        followingCountTextView.text = "${user.followingCount} Following"

        viewedUserId = user.id

        if (loggedInUserId == user.id) {
            actionButton.text = "Edit Profile"
            actionButton.visibility = View.VISIBLE
            actionButton.setOnClickListener {
                Toast.makeText(requireContext(), "跳转到编辑页面", Toast.LENGTH_SHORT).show()
            }
        } else {
            checkFollowingStatus(loggedInUserId, user.id!!)
        }
    }

    //Do not touch methods from here onwards(Hannah)
    private fun checkFollowingStatus(userId: Long, targetUserId: Long) {
        RetrofitClient.instance.getFollowing(userId).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val following = response?.body()
                    isFollowing = following?.any {it.id == targetUserId} ?: false
                    actionButton.text = if (isFollowing) "Unfollow" else "Follow"
                    actionButton.visibility = View.VISIBLE

                    actionButton.setOnClickListener {
                        if (isFollowing) unfollowUser(userId, targetUserId)
                        else followUser(userId, targetUserId)
                    }
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to check following status", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun followUser(userId: Long, targetUserId: Long) {
        RetrofitClient.instance.followUser(userId, targetUserId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    isFollowing = true
                    actionButton.text = "Unfollow"
                    getFollowersCount(targetUserId)
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    private fun unfollowUser(userId: Long, targetUserId: Long) {
        RetrofitClient.instance.unfollowUser(userId, targetUserId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    isFollowing = false
                    actionButton.text = "Follow"
                    getFollowersCount(targetUserId)
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    private fun getFollowingCount(userId: Long) {
        RetrofitClient.instance.getFollowing(userId).enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.isSuccessful) {
                        followingCountTextView.text = "${response.body()!!.size} Following"
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to update following count", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getFollowersCount(userId: Long) {
        RetrofitClient.instance.getFollowers(userId).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    followersCountTextView.text = "${response.body()!!.size} Followers"
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to update followers count", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logout() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No valid session found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.logout("$token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
