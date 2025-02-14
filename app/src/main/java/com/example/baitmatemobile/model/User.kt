package com.example.baitmatemobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Long?,
    val username: String?,
    val phoneNumber: String?,
    val email: String?,
    val age: Int?,
    val gender: String?,
    val address: String?,
    val joinDate: String?,
    val userStatus: String?,
    val savedPosts: List<Post>?,
    val savedLocations: List<FishingLocation>?,
    val likedPosts: List<Post>?,
) : Parcelable
