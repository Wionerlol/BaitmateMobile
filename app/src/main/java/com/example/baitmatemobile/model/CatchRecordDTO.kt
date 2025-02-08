package com.example.baitmatemobile.model

data class CatchRecordDTO(
    val time: String,
    val image: ByteArray,
    val length: Double,
    val weight: Double,
    val latitude: Double,
    val longitude: Double,
    val remark: String,
    val fishId: Long,
    val userId: Long,
    val locationId: Long
)