package com.example.baitmatemobile.model

import java.time.LocalDateTime

data class Post(
    val id: Long,
    val postTitle: String,
    val postContent: String,
    val postStatus: String,
    val location: String,
    val postTime: LocalDateTime,
    val likeCount: Int,
    val savedCount: Int,
    val accuracyScore: Double,
    val user: User
)
