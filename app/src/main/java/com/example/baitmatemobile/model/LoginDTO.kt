package com.example.baitmatemobile.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)
data class ErrorResponse(
    val errorMessage: Any
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

data class RegisterRequest(
    val username: String,
    val password: String,
    val phoneNumber: String?,
    val email: String,
    val age: Int,
    val gender: String?, // Optional
    val address: String? // Optional
)
