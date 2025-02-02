package com.example.baitmatemobile.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val id: Long?,
    val username: String?,
    val email: String?,
    val userStatus: String?,
    val error: String? = null  // Optional field for error message
)
