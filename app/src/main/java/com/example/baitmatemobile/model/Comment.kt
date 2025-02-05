package com.example.baitmatemobile.model

data class Comment(
    val id: Long?,
    val comment: String?,
    // 后端 LocalDateTime => 前端 String
    val time: String?,
    val likeCount: Int?,

    // 评论对应的用户
    val user: User?,

    // 如果需要 postId
    val postId: Long?
)
