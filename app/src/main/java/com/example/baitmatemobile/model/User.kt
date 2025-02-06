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

    val followingCount: Int? = 0,
    val followersCount: Int? = 0,

    val profileImage: ByteArray?
)
