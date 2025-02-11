package com.example.baitmatemobile.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baitmatemobile.fragment.CatchRecordFragment
import com.example.baitmatemobile.fragment.PostsFragment

class ProfileTabsAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment){
    override fun getItemCount(): Int =2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PostsFragment()
            1 -> CatchRecordFragment()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}