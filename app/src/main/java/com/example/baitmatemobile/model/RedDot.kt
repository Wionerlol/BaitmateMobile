package com.example.baitmatemobile.model

sealed class RedDot(
    open val id: Long,
    open val senderId: Long,
    open val postId: Long,
    open val time: String,
    var sender: User? = null,
    var post: Post? = null
) {
    abstract fun getMessage(): String
}

data class CommentRedDot(
    override val id: Long,
    override val senderId: Long,
    override val postId: Long,
    override val time: String,
    val commentText: String
) : RedDot(id, senderId, postId, time) {
    override fun getMessage(): String {
        return "${sender?.username} commented your post: ${post?.postTitle}"
    }
}

data class LikeRedDot(
    override val id: Long,
    override val senderId: Long,
    override val postId: Long,
    override val time: String
) : RedDot(id, senderId, postId, time) {
    override fun getMessage(): String {
        return "${sender?.username} liked your post: ${post?.postTitle}"
    }
}

data class RedDotResponse(
    val id: Long,
    val senderId: Long,
    val postId: Long,
    val time: String,
    val type: String,
    val commentText: String?
) {
    fun toRedDot(): RedDot {
        return when (type) {
            "COMMENT" -> CommentRedDot(id, senderId, postId, time, commentText ?: "")
            "LIKE" -> LikeRedDot(id, senderId, postId, time)
            else -> throw IllegalArgumentException("Unknown notification type: $type")
        }
    }
}