package com.example.baitmatemobile.network


import com.example.baitmatemobile.model.LoginRequest
import com.example.baitmatemobile.model.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
        @POST("/api/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>

        @POST("/api/logout")
        fun logout(@Header("Authorization") token: String): Call<ResponseBody>
}