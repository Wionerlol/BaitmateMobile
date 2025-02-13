package com.example.baitmatemobile.model

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Strings

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

data class CatchRecord(
    val id: Long,
    val time: String,
    val length: Double?,
    val weight: Double?,
    val fishName: String?,
    val locationName: String?
)