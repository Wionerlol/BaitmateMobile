package com.example.baitmatemobile.network


import com.example.baitmatemobile.model.CreateCommentDTO
import com.example.baitmatemobile.model.CreatedPostDTO
import com.example.baitmatemobile.activity.CatchDetailActivity
import com.example.baitmatemobile.activity.UploadPostActivity
import com.example.baitmatemobile.model.CatchRecord
import com.example.baitmatemobile.model.CatchRecordDTO
import com.example.baitmatemobile.model.FishingLocation
import com.example.baitmatemobile.model.ForgotPasswordRequest
import com.example.baitmatemobile.model.LocationDTO
import com.example.baitmatemobile.model.LoginRequest
import com.example.baitmatemobile.model.LoginResponse
import com.example.baitmatemobile.model.RedDotResponse
import com.example.baitmatemobile.model.Post
import com.example.baitmatemobile.model.PostReportRequest
import com.example.baitmatemobile.model.RegisterRequest
import com.example.baitmatemobile.model.ResetPasswordRequest
import com.example.baitmatemobile.model.User
import com.google.android.gms.common.internal.safeparcel.SafeParcelable
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
        fun getFishingLocations(): Call<List<FishingLocation>>

        @GET("saved-locations/saved")
        suspend fun getSavedLocations(@Query("userId") userId: Long): List<FishingLocation>

        @POST("saved-locations/save")
        fun saveLocation(@Query("userId") userId: Long, @Query("locationId") locationId: Long): Call<ResponseBody>

        @POST("saved-locations/remove")
        fun removeLocation(@Query("userId") userId: Long, @Query("locationId") locationId: Long): Call<ResponseBody>

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

        // 获取所有 Post
        @GET("posts")
        suspend fun getAllPosts(): List<Post>

        // 获取单个 Post
        @GET("posts/{id}")
        suspend fun getPostById(@Path("id") id: Long): Post

        @GET("posts/{id}/by-user")
        suspend fun getPostByIdWithUser(@Path("id") id: Long,
                                @Query("userId") userId: Long): Post

        @GET("users/{id}")
        suspend fun getUserById(@Path("id") id: Long): User

        // 创建 Post
        @POST("posts/create")
        suspend fun createPost(@Body post: CreatedPostDTO): Long

        // 更新喜爱状态
        @PUT("posts/{postId}/like")
        suspend fun toggleLike(
                @Path("postId") postId: Long,
                @Query("userId") userId: Long
        ): Post

        @Multipart
        @POST("http://10.0.2.2:5000/api/image/predict")
        suspend fun uploadImage(
                @Part image: MultipartBody.Part
        ): Response<List<List<String>>>

        @GET("locations")
        suspend fun getLocations(): Response<List<LocationDTO>>

        @POST("catch-records/add")
        suspend fun addCatchRecord(@Body record: CatchRecordDTO): Response<Unit>

        @PUT("posts/{postId}/save")
        suspend fun toggleSave(
                @Path("postId") postId: Long,
                @Query("userId") userId: Long
        ): Post

        @GET("catch-records/user/{userId}")
        suspend fun getCatchRecordsByUserId(@Path ("userId") userId: Long): List<CatchRecord>

        @POST("posts/comment")
        suspend fun createComment(@Body comment: CreateCommentDTO): Long

        @GET("posts/user/{userId}")
        suspend fun getPostsByUserId(@Path("userId") userId: Long): List<Post>

        @GET("user/{userId}")
        suspend fun getUserDetails(@Path ("userId") userId: Long): Response<User>

        @GET("user/{userId}/followers")
        suspend fun getFollowers(@Path("userId") userId: Long): List<User>

        @GET("user/{userId}/following")
        suspend fun getFollowing(@Path("userId") userId: Long): List<User>

        @POST("user/{userId}/follow")
        suspend fun followUser(@Path("userId") userId: Long, @Query("targetUserId") targetUserId: Long): Response<ResponseBody>

        @POST("user/{userId}/unfollow")
        suspend fun unfollowUser(@Path("userId") userId: Long, @Query("targetUserId") targetUserId: Long): Response<ResponseBody>

        @POST("http://10.0.2.2:5000/api/image/check")
        suspend fun checkImage(
                @Body image: UploadPostActivity.ImageCheckRequest
        ): Response<UploadPostActivity.ImageCheckResponse>

        @GET("redDots/{userId}")
        suspend fun getRedDots(@Path("userId") userId: Long): List<RedDotResponse>

        @POST("posts/report")
        suspend fun reportPost(@Body request: PostReportRequest)

}
