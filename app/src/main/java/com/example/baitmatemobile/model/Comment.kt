package com.example.baitmatemobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: Long?,
    val comment: String?,
    val time: String?,
    val likeCount: Int?,
    val user: User?,
    val postId: Long?
): Parcelable
