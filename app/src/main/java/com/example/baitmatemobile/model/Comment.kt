package com.example.baitmatemobile.model

data class Comment(
    val id: Long?,
    val comment: String?,
    val time: String?,
    val likeCount: Int?,
    val user: User?,
    val postId: Long?
)
