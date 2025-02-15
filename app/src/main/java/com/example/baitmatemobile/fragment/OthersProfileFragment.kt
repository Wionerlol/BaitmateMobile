package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build.VERSION_CODES.BASE
import android.os.Bundle
import android.util.Log
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.baitmatemobile.R
import com.example.baitmatemobile.activity.LoginActivity
import com.example.baitmatemobile.adapter.ProfileTabsAdapter
import com.example.baitmatemobile.model.User
import com.example.baitmatemobile.network.RetrofitClient
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.checkerframework.checker.units.qual.t
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.io.encoding.ExperimentalEncodingApi

class OthersProfileFragment : Fragment() {
    private lateinit var userNameTextView: TextView
    private lateinit var userInfoTextView: TextView
    private lateinit var followingCountTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var btnAction: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var profileImage: ImageView
    private var userId: Long = -1
    private var viewedUserId: Long =-1
    private var isFollowing = false

    companion object {
        fun newInstance(userId: Long): OthersProfileFragment {
            val fragment = OthersProfileFragment()
            val args = Bundle()
            args.putLong("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_others_profile, container, false)

        userNameTextView = view.findViewById(R.id.userName)
        userInfoTextView = view.findViewById(R.id.userInfo)
        followingCountTextView = view.findViewById(R.id.followingCount)
        followersCountTextView = view.findViewById(R.id.followersCount)
        btnAction = view.findViewById(R.id.btnAction)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)

        val sharedPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getLong("userId", -1)

        arguments?.let {
            viewedUserId = it.getLong("userId", -1)
        }

        val adapter = ProfileTabsAdapter(requireActivity(), viewedUserId)
        viewPager.adapter = adapter

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
        CoroutineScope(Dispatchers.IO).launch {
            val userDetailsDeferred = async { getUserDetails(viewedUserId) }
            val followersCountDeferred = async { getFollowersCount(viewedUserId) }
            val followingCountdeferred = async { getFollowingCount(viewedUserId)}

            val user = userDetailsDeferred.await()
            val followersCount = followersCountDeferred.await()
            val followingCount = followingCountdeferred.await()

            withContext(Dispatchers.Main) {
                userNameTextView.text = user?.username ?: "Unknown User"
                userInfoTextView.text = user?.email ?: "No email provided"
                followersCountTextView.text = "$followersCount Followers"
                followingCountTextView.text = "$followingCount Followers"
            }
            checkFollowingStatus(userId, viewedUserId)
        }
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
            val followersList = RetrofitClient.instance.getFollowers(userId)
            followersList.size
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Network error while fetching followers count", e)
            0
        }
    }


    private suspend fun getFollowingCount(userId: Long): Int {
        return try {
            val followingList = RetrofitClient.instance.getFollowing(userId)
            followingList.size
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Network error while fetching followers count", e)
            0
        }
    }

    private fun checkFollowingStatus(userId: Long, targetUserId: Long) {
        lifecycleScope.launch {
            try {
                val following = RetrofitClient.instance.getFollowing(userId)
                isFollowing = following.any { it.id == targetUserId }

                btnAction.text = if (isFollowing) "Unfollow" else "Follow"

                btnAction.setOnClickListener {
                    lifecycleScope.launch {
                        if (isFollowing) {
                            unfollowUser(userId, targetUserId)
                        } else {
                            followUser(userId, targetUserId)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Failed to check following status: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private suspend fun followUser(userId: Long, targetUserId: Long) {
        try {
            val response = RetrofitClient.instance.followUser(userId, targetUserId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    isFollowing = true
                    btnAction.text = "Unfollow"
                    val followersCount = getFollowersCount(targetUserId)
                    followersCountTextView.text = "$followersCount Followers"
                    Toast.makeText(requireContext(), "Successfully followed!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to follow: ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Response failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private suspend fun unfollowUser(userId: Long, targetUserId: Long) {
        try {
            val response = RetrofitClient.instance.unfollowUser(userId, targetUserId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    isFollowing = false
                    btnAction.text = "Follow"
                    val followersCount = getFollowersCount(targetUserId)
                    followersCountTextView.text = "$followersCount Followers"
                    Toast.makeText(requireContext(), "Successfully unfollowed!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to unfollow: ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Response failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}