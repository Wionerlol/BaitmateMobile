package com.example.baitmatemobile.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.baitmatemobile.R
import com.example.baitmatemobile.databinding.ActivityMainBinding
import com.example.baitmatemobile.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if(isLoggedIn) {
            replaceFragment(HomeFragment())
        } else {
            replaceFragment(LoginFragment())
        }

        // 绑定底部导航栏
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())  // ✅ 这里绑定 HomeFragment
                R.id.nav_map -> replaceFragment(MapFragment())
                R.id.nav_photograph -> replaceFragment(CameraFragment())
                R.id.nav_messages -> replaceFragment(MessagesFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
    }

    // 打开搜索页面
    fun openSearchActivity() {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    // 切换 Fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
