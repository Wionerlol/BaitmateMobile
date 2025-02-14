package com.example.baitmatemobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val id: Long?,
    val image: Long?,
): Parcelable