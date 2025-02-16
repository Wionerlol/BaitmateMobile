package com.example.baitmatemobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: Long?,
    val comment: String?,
    val time: String?,
    var likeCount: Int?,
    val user: User?,
    val postId: Long?,
    var likedByCurrentUser: Boolean?
): Parcelable
