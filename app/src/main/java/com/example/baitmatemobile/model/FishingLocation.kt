package com.example.baitmatemobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FishingLocation(
    val id: Long,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
) : Parcelable
