package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.LoginActivity
import com.example.baitmatemobile.adapter.ProfileTabsAdapter
import com.example.baitmatemobile.model.Image
import com.example.baitmatemobile.model.User
import com.example.baitmatemobile.network.RetrofitClient
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var profileImage: ImageView
    private var userId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImage = view.findViewById(R.id.profileImage)
        userNameTextView = view.findViewById(R.id.userName)
        userInfoTextView = view.findViewById(R.id.userInfo)
        followingCountTextView = view.findViewById(R.id.followingCount)
        followersCountTextView = view.findViewById(R.id.followersCount)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)

        val sharedPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getLong("userId", -1)

        // Set up ViewPager2 with the adapter
        val adapter = ProfileTabsAdapter(requireActivity(), userId)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Posts"
                1 -> tab.text = "Catch Record"
                2 -> tab.text = "Saved Posts"
            }
        }.attach()

        viewPager.offscreenPageLimit = 1

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("ProfileFragment", "$userId")
        CoroutineScope(Dispatchers.IO).launch {
            val userDetailsDeferred = async { getUserDetails(userId) }
            val followersCountDeferred = async { getFollowersCount(userId) }
            val followingCountdeferred = async { getFollowingCount(userId)}

            // Wait for both tasks to complete
            val user = userDetailsDeferred.await()
            val followersCount = followersCountDeferred.await()
            val followingCount = followingCountdeferred.await()

            withContext(Dispatchers.Main) {
                userNameTextView.text = user?.username ?: "Unknown User"
                userInfoTextView.text = user?.email ?: "No email provided"
                /*
                        user?.profileImage?.let { base64String ->
                            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            profileImage.setImageBitmap(bitmap)
                        } ?: run {
                            // Optionally set a placeholder image if the profileImage is null
                            profileImage.setImageResource(R.drawable.ic_touxiang)
                        }

                         */
                followersCountTextView.text = "$followersCount Followers"
                followingCountTextView.text = "$followingCount Following"
            }
        }
        btnLogout.setOnClickListener { logout() }
        btnEditProfile.setOnClickListener{ }
    }

    private suspend fun getUserDetails(userId: Long): User? {
        return try {
            val response = RetrofitClient.instance.getUserDetails(userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("ProfileFragment", "Failed to fetch user: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Network error while fetching user", e)
            null
        }
    }

    private suspend fun getFollowersCount(userId: Long): Int {
        return try {
            val followersList = RetrofitClient.instance.getFollowers(userId) // Suspend function
            followersList.size
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Network error while fetching followers count", e)
            0
        }
    }

    private suspend fun getFollowingCount(userId: Long): Int {
        return try {
            val followingList = RetrofitClient.instance.getFollowing(userId) // Suspend function
            followingList.size
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Network error while fetching followers count", e)
            0
        }
    }

    private fun logout() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "") ?: ""

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
