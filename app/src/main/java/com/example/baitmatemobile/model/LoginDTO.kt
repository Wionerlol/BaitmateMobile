package com.example.baitmatemobile.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val user: User,
    val token: String,
    val errorMessage: String? = null// Optional field for error message
)
