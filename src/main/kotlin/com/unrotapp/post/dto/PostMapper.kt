package com.unrotapp.post.dto

import com.unrotapp.post.model.Category
import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostComment

fun Post.toResponse() = PostResponse(
    id = id!!,
    authorId = authorId,
    type = type,
    content = content,
    category = category?.toResponse(),
    likeCount = likeCount,
    commentCount = commentCount,
    bookmarkCount = bookmarkCount,
    shareCount = shareCount,
    createdAt = createdAt
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
