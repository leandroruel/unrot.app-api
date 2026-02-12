package com.unrotapp.post.dto

import com.unrotapp.post.model.PostType
import java.time.Instant
import java.util.UUID

data class PostResponse(
    val id: UUID,
    val authorId: UUID,
    val type: PostType,
    val content: String?,
    val likeCount: Long,
    val commentCount: Long,
    val bookmarkCount: Long,
    val shareCount: Long,
    val createdAt: Instant
)


data class PostCommentResponse(
    val id: UUID,
    val postId: UUID,
    val userId: UUID,
    val content: String,
    val createdAt: Instant
)

data class CreatePostRequest(
    val type: PostType,
    val content: String?
)

data class CreateCommentRequest(
    val content: String
)
