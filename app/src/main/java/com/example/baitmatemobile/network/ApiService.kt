package com.example.baitmatemobile.network


import com.example.baitmatemobile.model.ForgotPasswordRequest
import com.example.baitmatemobile.model.LoginRequest
import com.example.baitmatemobile.model.LoginResponse
import com.example.baitmatemobile.model.ResetPasswordRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
        @POST("/api/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>

        @POST("/api/logout")
        fun logout(@Header("Authorization") token: String): Call<ResponseBody>

        @POST("/api/forgot-password")
        fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ResponseBody>

        @POST("/api/reset-password")
        fun resetPassword(@Body request: ResetPasswordRequest): Call<ResponseBody>

        @GET("/api/validate-token")
        fun validateToken(@Query("token") token: String): Call<Map<String, String>>
}