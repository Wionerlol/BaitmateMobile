package com.example.baitmatemobile.model

import java.time.LocalDateTime

data class Post(
    val id: Long?,
    val postTitle: String?,
    val postContent: String?,
    val postStatus: String?,

    val postTime: String?,

    var likeCount: Int?,
    var savedCount: Int?,
    val accuracyScore: Double?,


    val user: User?,

    // 位置（字符串）
    val location: String?,

    // 评论列表 (对应 CommentDto)
    val comments: List<Comment>?,

    // 图片列表 (对应 ImageDto)
    val images: List<Image>?,

    // 如果要显示被多少人收藏
    val savedByCount: Int?,

    var likedByCurrentUser: Boolean,
    var savedByCurrentUser: Boolean
)

data class CreatedPostDTO(

    val postTitle: String?,
    val postContent: String?,
    val userId: Long?,
    val location: String?,
    val imageBase64List: List<String>

)

data class CreateCommentDTO(
    val comment: String?,
    val postId: Long?,
    val userId: Long?
)

