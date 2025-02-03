package com.example.baitmatemobile.model

import java.time.LocalDate

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val userStatus: String,
    val profileImage: String?, // Base64 或 URL
    val joinDate: LocalDate,
    val savedPosts: List<Post>?
)
