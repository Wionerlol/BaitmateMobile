package com.example.baitmatemobile.model

import java.time.LocalDateTime

data class Post(
    val id: Long?,
    val postTitle: String?,
    val postContent: String?,
    val postStatus: String?,
    // 后端是 LocalDateTime，这里前端先用 String?
    val postTime: String?,

    var likeCount: Int?,
    val savedCount: Int?,
    val accuracyScore: Double?,

    // 用户信息
    val user: User?,

    // 位置（字符串）
    val location: String?,

    // 评论列表 (对应 CommentDto)
    val comments: List<Comment>?,

    // 图片列表 (对应 ImageDto)
    val images: List<Image>?,

    // 如果要显示被多少人收藏
    val savedByCount: Int?,

    val likedByCurrentUser: Boolean
)

