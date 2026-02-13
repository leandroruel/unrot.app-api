package com.unrotapp.post.dto

import com.unrotapp.post.model.Category
import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostComment
import com.unrotapp.post.model.PostMedia

fun Post.toResponse(media: List<PostMedia> = emptyList()) = PostResponse(
    id = id!!,
    authorId = authorId,
    type = type,
    content = content,
    category = category?.toResponse(),
    media = media.map { it.toResponse() },
    likeCount = likeCount,
    commentCount = commentCount,
    bookmarkCount = bookmarkCount,
    shareCount = shareCount,
    createdAt = createdAt
)

fun PostMedia.toResponse() = PostMediaResponse(
    id = id!!,
    url = url,
    originalFilename = originalFilename,
    mimeType = mimeType,
    fileSizeBytes = fileSizeBytes
)

fun Category.toResponse() = CategoryResponse(
    id = id!!,
    name = name,
    slug = slug
)

fun PostComment.toResponse() = PostCommentResponse(
    id = id!!,
    postId = postId,
    userId = userId,
    content = content,
    createdAt = createdAt
)
