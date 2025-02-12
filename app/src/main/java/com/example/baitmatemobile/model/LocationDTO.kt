package com.example.baitmatemobile.model

import com.google.gson.annotations.SerializedName

data class LocationDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("locationName") val locationName: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)