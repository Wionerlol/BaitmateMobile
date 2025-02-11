package com.example.baitmatemobile.model

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
    val profileImage: ByteArray?,
    val savedPosts: List<Any>?,
    val savedLocations: List<Any>?,
    val likedPosts: List<Any>?,
    val following: List<Any>?
)
