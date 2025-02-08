package com.example.baitmatemobile.network


import com.example.baitmatemobile.activity.CatchDetailActivity
import com.example.baitmatemobile.model.CatchRecordDTO
import com.example.baitmatemobile.model.FishingLocation
import com.example.baitmatemobile.model.ForgotPasswordRequest
import com.example.baitmatemobile.model.LoginRequest
import com.example.baitmatemobile.model.LoginResponse
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.model.RegisterRequest
import com.example.baitmatemobile.model.ResetPasswordRequest
import com.example.baitmatemobile.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
        fun saveCatchRecord(@Body catchRecord: CatchRecordDTO): Call<Void>


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

        // ✅ 获取所有 Post
        @GET("posts")
        suspend fun getAllPosts(): List<Post>

        // ✅ 获取单个 Post
        @GET("posts/{id}")
        suspend fun getPostById(@Path("id") id: Long): Post

        // ✅ 创建 Post
        @POST("posts")
        suspend fun createPost(@Body post: Post): Post

        // ✅ 更新喜爱状态
        @PUT("posts/{postId}/like")
        suspend fun toggleLike(
                @Path("postId") postId: Long,
                @Query("userId") userId: Long
        ): Post

        // ✅ 获取当前登录用户信息
        @GET("user/profile")
        fun getUserProfile(@Header("Authorization") token: String): Call<User>

        // ✅ 获取某个用户的关注人数（可用于不同用户）
        @GET("user/{userId}/following/count")
        fun getFollowingCount(
                @Header("Authorization") token: String,
                @Path("userId") userId: Long
        ): Call<Int>

        // ✅ 获取某个用户的粉丝数（可用于不同用户）
        @GET("user/{userId}/followers/count")
        fun getFollowersCount(
                @Header("Authorization") token: String,
                @Path("userId") userId: Long
        ): Call<Int>

        // ✅ 获取某个用户关注的人列表
        @GET("user/{userId}/following")
        fun getFollowingList(
                @Header("Authorization") token: String,
                @Path("userId") userId: Long
        ): Call<List<User>>

        // ✅ 获取某个用户的粉丝列表
        @GET("user/{userId}/followers")
        fun getFollowersList(
                @Header("Authorization") token: String,
                @Path("userId") userId: Long
        ): Call<List<User>>


        @GET("user/{userId}/profile")
        fun getUserProfileById(@Header("Authorization") token: String, @Path("userId") userId: Long): Call<User>

        @GET("user/{targetUserId}/isFollowing")
        fun isFollowing(@Header("Authorization") token: String, @Path("targetUserId") targetUserId: Long): Call<Boolean>

        @POST("user/{targetUserId}/follow")
        fun followUser(@Header("Authorization") token: String, @Path("targetUserId") targetUserId: Long): Call<ResponseBody>

        @POST("user/{targetUserId}/unfollow")
        fun unfollowUser(@Header("Authorization") token: String, @Path("targetUserId") targetUserId: Long): Call<ResponseBody>

        @Multipart
        @POST("http://10.0.2.2:5000/api/image/predict")
        suspend fun uploadImage(
                @Part image: MultipartBody.Part
        ): Response<List<List<String>>>

        @GET("locations")
        suspend fun getLocations(): Response<List<CatchDetailActivity.LocationDTO>>

        @POST("catch-records/add")
        suspend fun addCatchRecord(@Body record: CatchRecordDTO): Response<Unit>
}
