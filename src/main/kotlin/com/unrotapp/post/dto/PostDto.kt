package com.unrotapp.post.dto

import com.unrotapp.post.model.PostType
import java.time.Instant
import java.util.UUID

data class PostResponse(
    val id: UUID,
    val authorId: UUID,
    val type: PostType,
    val content: String?,
    val category: CategoryResponse?,
    val media: List<PostMediaResponse>,
    val likeCount: Long,
    val commentCount: Long,
    val bookmarkCount: Long,
    val shareCount: Long,
    val createdAt: Instant
)

data class PostMediaResponse(
    val id: UUID,
    val url: String,
    val originalFilename: String?,
    val mimeType: String,
    val fileSizeBytes: Long
)

data class CategoryResponse(
    val id: UUID,
    val name: String,
    val slug: String
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
    val content: String?,
    val categorySlug: String? = null
)

data class CreateCommentRequest(
    val content: String
)
