package com.example.baitmatemobile.network


import com.example.baitmatemobile.model.LoginRequest
import com.example.baitmatemobile.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
        @POST("/api/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>
}