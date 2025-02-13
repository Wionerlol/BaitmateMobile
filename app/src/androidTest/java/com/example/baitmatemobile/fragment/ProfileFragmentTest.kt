package com.example.baitmatemobile.fragment

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.example.baitmatemobile.activity.TestActivity
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ProfileFragmentTest {

    @get:Rule
    val activityRule = ActivityTestRule(TestActivity::class.java)

    @Test
    fun testProfileFragmentLaunchesSuccessfully() {
        val activity = activityRule.activity

        // 设置模拟 SharedPreferences
        val sharedPrefs = activity.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putLong("userId", 12345L).apply()

        val fragment = ProfileFragment()

        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNowAllowingStateLoss() // 使用 commitNowAllowingStateLoss 确保 fragment 加载
        }

        // 强制让 Fragment 的视图完成渲染
        activity.runOnUiThread {
            fragment.requireView().post {
                assertNotNull(fragment.view)
            }
        }
    }

}
