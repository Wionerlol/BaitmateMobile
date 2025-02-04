package com.example.baitmatemobile.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)
data class ErrorResponse(
    val errorMessage: String
)

data class LoginResponse(
    val userId: Long,
    val token: String,
    val errorMessage: String? = null// Optional field for error message
)

data class ForgotPasswordRequest(
    val username: String,
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)
