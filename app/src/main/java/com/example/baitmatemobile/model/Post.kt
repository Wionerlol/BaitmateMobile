package com.example.baitmatemobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
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
    val location: String?,

    val comments: List<Comment>?,
    val images: List<Image>?,

    var likedByCurrentUser: Boolean,
    var savedByCurrentUser: Boolean
): Parcelable

data class CreatedPostDTO(

    val postTitle: String?,
    val postContent: String?,
    val userId: Long?,
    val location: String?,
    val status: String?,
    val imageBase64List: List<String>,
    val accuracyScore: Double?

)

data class CreateCommentDTO(
    val comment: String?,
    val postId: Long?,
    val userId: Long?
)

data class PostReportRequest(val postId: Long)

