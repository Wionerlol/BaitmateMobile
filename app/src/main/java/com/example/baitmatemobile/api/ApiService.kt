package com.example.baitmatemobile.api

import com.example.baitmatemobile.model.Post
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
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
