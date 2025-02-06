package com.example.baitmatemobile.network


import com.example.baitmatemobile.model.CatchRecord
import com.example.baitmatemobile.model.FishingLocation
import com.example.baitmatemobile.model.ForgotPasswordRequest
import com.example.baitmatemobile.model.LoginRequest
import com.example.baitmatemobile.model.LoginResponse
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.model.RegisterRequest
import com.example.baitmatemobile.model.ResetPasswordRequest
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
        @GET("locations")
        fun getFishingLocations(): Call<JSONArray>

        @GET("locations/{id}")
        suspend fun getFishingSpotById(@Path("id") id: Long): FishingLocation

        @GET("locations/search")
        suspend fun searchFishingSpots(@Query("query") query: String): List<FishingLocation>

        @GET("locations/nearby")
        suspend fun getNearbyFishingSpots(
                @Query("latitude") latitude: Double,
                @Query("longitude") longitude: Double,
                @Query("radius") radius: Double = 5.0
        ): List<FishingLocation>

        @GET("locations/suggestions")
        suspend fun getSearchSuggestions(@Query("query") query: String): List<String>

        @POST("login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>

        @POST("/api/catch-records/add")
        fun saveCatchRecord(@Body catchRecord: CatchRecord): Call<Void>


        @POST("logout")
        fun logout(@Header("Authorization") token: String): Call<ResponseBody>

        @POST("forgot-password")
        fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ResponseBody>

        @POST("reset-password")
        fun resetPassword(@Body request: ResetPasswordRequest): Call<ResponseBody>

        @GET("validate-token")
        fun validateToken(@Query("token") token: String): Call<Map<String, String>>

        @POST("register")
        fun register(@Body request: RegisterRequest): Call<ResponseBody>

        // 获取所有 Post
        @GET("posts")
        suspend fun getAllPosts(): List<Post>

        // 获取单个 Post
        @GET("posts/{id}")
        suspend fun getPostById(@Path("id") id: Long): Post

        // 创建 Post
        @POST("posts")
        suspend fun createPost(@Body post: Post): Post

        // 更新喜爱状态
        @PUT("posts/{postId}/like")
        suspend fun toggleLike(
                @Path("postId") postId: Long,
                @Query("userId") userId: Long
        ): Post
}