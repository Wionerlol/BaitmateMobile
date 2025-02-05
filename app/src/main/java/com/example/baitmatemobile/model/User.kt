package com.example.baitmatemobile.model

import java.time.LocalDate

data class User(
    val id: Long?,
    val username: String?,
    val phoneNumber: String?,
    val email: String?,
    val age: Int?,
    val gender: String?,
    val address: String?,
    // 后端 LocalDate => 前端 String
    val joinDate: String?,
    val userStatus: String?,

    // 如果后端直接返回 byte[]
    val profileImage: ByteArray?
)
