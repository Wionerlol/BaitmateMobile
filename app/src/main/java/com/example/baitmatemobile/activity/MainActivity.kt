package com.example.baitmatemobile.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.baitmatemobile.R
import com.example.baitmatemobile.databinding.ActivityMainBinding
import com.example.baitmatemobile.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val mapFragment = MapFragment()
    private val cameraFragment = CameraFragment()
    private val messagesFragment = MessageFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, homeFragment, "home")
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, mapFragment, "map")
            .hide(mapFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, cameraFragment, "camera")
            .hide(cameraFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, messagesFragment, "messages")
            .hide(messagesFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, profileFragment, "profile")
            .hide(profileFragment)
            .commit()

        // 绑定底部导航栏
        binding.bottomNav.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            when (item.itemId) {
                R.id.nav_home -> showFragment(homeFragment)
                R.id.nav_map -> showFragment(mapFragment)
                R.id.nav_photograph -> showFragment(cameraFragment)
                R.id.nav_messages -> showFragment(messagesFragment)
                R.id.nav_profile -> showFragment(profileFragment)
            }
            true
        }
    }

    // 打开搜索页面
    fun openSearchActivity() {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    // 切换 Fragment
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            // Hide all fragments.
            hide(homeFragment)
            hide(mapFragment)
            hide(cameraFragment)
            hide(messagesFragment)
            hide(profileFragment)
            // Show the selected fragment.
            show(fragment)
        }.commit()
    }
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Pop the top fragment in the backstack
            supportFragmentManager.popBackStack()
        } else {
            // If no fragments in backstack, perform default back behavior
            super.onBackPressed()
        }
    }

}
